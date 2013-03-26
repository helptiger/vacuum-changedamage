package vacuum.changedamage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import net.minecraft.server.v1_5_R2.MobEffect;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import vacuum.changedamage.equations.ExpressionParser;
import vacuum.changedamage.equations.PostfixNotation;
import vacuum.changedamage.equations.element.number.Variable;
import vacuum.changedamage.equations.element.number.VariablePool;
import vacuum.changedamage.hooks.ArmorHook;
import vacuum.changedamage.hooks.PotionHook;
import vacuum.changedamage.listener.DamageListener;
import vacuum.changedamage.listener.FallListener;

public class ChangeDamagePlugin extends JavaPlugin{

	private static final String fileRepository = "http://vacuum-changedamage.googlecode.com/svn/trunk/resources/";
	/*	private static final String experimentalUpdateRepository = fileRepository + "jars/experimental/";
	private static final String stableUpdateRepository = fileRepository + "jars/stable/";
	private static final String updateJAR = "ChangeDamage.jar";
	private static final String updateVersion = "version.txt";*/

	public static boolean research = false;
	private DamageListener dl;
	public static boolean verbose;
	//private static final String idFile = "items.txt";
	private static final String potionEffectFile = "potioneffects.txt";
	private static final String potionIDFile = "potions.txt";

	private ArmorHook armorHook;
	private FallListener fl;

	private PotionHook potionHook;

	@Override
	public void onEnable() {

		//removed due to Bukkit Dev safety rules
		/*if(update())
			return;*/

		getDataFile("config.yml", false);

		boolean b  = false;
		if(!getConfig().contains("pvponly")){
			getConfig().createSection("pvponly");
			getConfig().set("pvponly", true);
			b = true;
		}

		if(!getConfig().contains("verbose")){
			getConfig().createSection("verbose");
			getConfig().set("verbose", false);
			b = true;
		}

		if(getConfig().contains("research")){
			getConfig().set("research", null);
			b = true;
		}

		if(!getConfig().contains("damages")){
			getConfig().createSection("damages");
			b = true;
		}

		if(!getConfig().contains("damages.default")){
			getConfig().createSection("damages.default");

			//put some values in
			getConfig().createSection("damages.default.DIAMOND_SWORD");
			getConfig().set("damages.default.DIAMOND_SWORD", 9);
			b = true;
		}

		if(!getConfig().contains("damages.expression")){
			getConfig().createSection("damages.expression");
			b = true;
		}

		if(!getConfig().contains("damages.expression.critical") || getConfig().getString("damages.expression.critical").equals("i 2 / 2 + rand * fl")){
			getConfig().createSection("damages.expression.critical");
			getConfig().set("damages.expression.critical", "i i 2 / 2 + rand * fl +");
			b = true;
		}

		if(!getConfig().contains("damages.expression.weakness")){
			getConfig().createSection("damages.expression.weakness");
			getConfig().set("damages.expression.weakness", "i 2 w << -");
			b = true;
		}

		if(!getConfig().contains("damages.expression.strength")){
			getConfig().createSection("damages.expression.strength");
			getConfig().set("damages.expression.strength", "i 3 s << +");
			b = true;
		}

		if(!getConfig().contains("armor")){
			getConfig().createSection("armor");
			b = true;
		}

		if(!getConfig().contains("armor.default")){
			getConfig().createSection("armor.default");

			//put some values in
			getConfig().createSection("armor.default.DIAMOND_CHESTPLATE");
			getConfig().set("armor.default.DIAMOND_CHESTPLATE", 8);
			b = true;
		}

		if(!getConfig().contains("fall")){
			getConfig().createSection("fall");
			b = true;
		}

		if(!getConfig().contains("fall.expression")){
			getConfig().createSection("fall.expression");
			getConfig().set("fall.expression", "d 3 - a 0 * +");
			b = true;
		}

		if(!getConfig().contains("potion")){
			getConfig().createSection("potion");
			getConfig().createSection("potion.0");
			b = true;
		}

		if(b)
			try {
				getConfig().save(getDataFile("config.yml", false));
			} catch (IOException e) {
				e.printStackTrace();
			}

		verbose = getConfig().getBoolean("verbose", false);
		dl = new DamageListener();
		fl = new FallListener();
		try {
			armorHook = new ArmorHook();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		/*potionListener = new PotionListener();*/
		reload();
		getServer().getPluginManager().registerEvents(dl, this);
		//getServer().getPluginManager().registerEvents(pjl, this);
		getServer().getPluginManager().registerEvents(fl, this);
		/*getServer().getPluginManager().registerEvents(potionListener, this);*/
	}

	private void reload() {
		/* unload */
		dl.clear();
		armorHook.restore();
		//if(potionHook != null) //FIXME: remove comment
			//potionHook.releaseHook();
		
		/* load */
		loadDamageMap();
		loadArmor();
		loadDamageEquations();
		loadFall();
		try{
			//loadPotionEffects();//FIXME: remove comment
		} catch (Exception ex){
			ex.printStackTrace();
		}

		boolean pvpOnly = getConfig().getBoolean("pvponly", true);
		dl.setPVPOnly(pvpOnly);
		dl.setVerbose(verbose);
		//		dl.setDefaultDamage(null, getConfig().getInt("defaultdamage", -1));
	}

	private void loadFall() {
		System.out.println("[ChangeDamage] Loading fall expression");

		VariablePool pool = new VariablePool(false);
		Variable d = pool.register("d", 0);
		Variable a = pool.register("a", 0);
		String eq = getConfig().getString("fall.expression", "");
		PostfixNotation expression = ExpressionParser.parsePostfix(eq, pool);
		fl.setExpression(expression, d, a);

		fl.setEventPriority(getConfig().getString("fall.priority"));

		System.out.println("[ChangeDamage] Successfully loaded fall damage");
	}

	private void loadDamageEquations() {
		System.out.println("[ChangeDamage] Loading damage expressions");
		VariablePool pool = dl.pool;
		String[] expressions = {
				"critical",
				"strength",
				"weakness",
		};
		for (String string : expressions) {
			String expression = getConfig().getString("damages.expression." + string);
			if(verbose)
				System.out.println("[ChangeDamage] Loading " + string + " expression: " + expression);
			dl.setEquation(string, ExpressionParser.parsePostfix(expression, pool));
		}
		System.out.println("[ChangeDamage] Successfully loaded damage expressions");
		/*PostfixNotation notation = ExpressionParser.parsePostfix(eq, pool);
		if(pjl == null)
			pjl = new PlayerJoinListener(notation, n);
		else
			pjl.open(notation, n);*/
	}

	private void loadPotionEffects() {
		System.out.println("[" + getDescription().getName() + "] Loading potion effects.");
		HashMap<Integer, List<MobEffect>> customEffects = new HashMap<Integer, List<MobEffect>>();
		ConfigurationSection section = getConfig().getConfigurationSection("potion");
		for(String str : section.getKeys(false)){ /* load each potion ID */
			int id = getID(str, potionIDFile);
			System.out.println("[ChangeDamage] Loading potion with ID: " + id);
			ConfigurationSection sub = section.getConfigurationSection(str);
			Set<String> keys = sub.getKeys(false);
			List<MobEffect> effects = new ArrayList<MobEffect>(keys.size());
			for(String string : keys){ /* load effects */
				ConfigurationSection subsub = sub.getConfigurationSection(string);
				int effectID = getID(string, potionEffectFile);
				int amplifier = 1;
				int duration = 0;

				if(subsub.contains("amplifier")){
					amplifier = subsub.getInt("amplifier");
				} else
					System.out.println("[ChangeDamage] WARNING! Amplifier not specified for potion ID: " + id + "; effect ID: " + effectID);

				if(subsub.contains("duration")){
					duration = subsub.getInt("duration");
				} else
					System.out.println("[ChangeDamage] WARNING! Duration not specified for potion ID: " + id + "; effect ID: " + effectID);
				
				if(verbose)
					System.out.println("[ChangeDamage] Adding effect: " + effectID + "; amplifier: " + amplifier + "; duration: " + duration);
				
				MobEffect effect = new MobEffect(effectID, amplifier, duration);
				effects.add(effect);
			}
			customEffects.put(id, effects);
		}
		potionHook = new PotionHook(customEffects);
		if(verbose){
			System.out.println("Successful: " + (net.minecraft.server.v1_5_R2.Item.POTION instanceof PotionHook));
		}
		System.out.println("[" + getDescription().getName() + "] Successfully loaded potion effects!");

	}

	private void loadArmor() {
		ConfigurationSection section = getConfig().getConfigurationSection("armor");
		for(String s : section.getKeys(false)){
			ConfigurationSection sub = section.getConfigurationSection(s);
			World w = (s.equals("default")) ? null : getServer().getWorld(s);
			System.out.println("[" + getDescription().getName() + "] Loading armor modifications for world " + ((w == null) ? "default" : w.getName()));
			for(String str : sub.getKeys(false)){
				try{
					int i;
					try{
						i = Integer.parseInt(str);
					} catch (NumberFormatException ex){
						try{
						i = Material.getMaterial(str).getId();
						} catch (NullPointerException ex2) {
							System.err.println("Error parsing: " + str + ": " + sub.getString(str));
							ex2.printStackTrace();
							continue;
						}
					}
					armorHook.modifyArmorValue(i, sub.getInt(str));
				} catch (Exception ex){
					ex.printStackTrace();
					System.out.println("[" + getDescription().getName() + "] Configuration node armor." + s + "." + str + " is causing an issue.");
				}
			}
		}
		System.out.println("[" + getDescription().getName() + "] Successfully loaded armor modifications!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(command.getName().equalsIgnoreCase("changedamage")){
			if(args == null || args.length == 0)
				return false;
			if(args[0].equalsIgnoreCase("release")){
				this.getPluginLoader().disablePlugin(this);
			} else if(args[0].equalsIgnoreCase("reload")){
				reload();
			} else
				return false;
			return true;
		}
		return false;
	}

	public int getID(String name, String file){

		try{
			return Integer.parseInt(name);
		} catch (NumberFormatException ex){
			//ignore b/c it means this isn't an ID, it's a name
		}
		File f = getDataFile(file, true);
		try {
			Scanner s = new Scanner(f);
			String line;
			while(s.hasNext()){
				line = s.nextLine();
				try{
					int i = Integer.parseInt(line.substring(0, line.indexOf(' ')));
					String n = line.substring(line.indexOf(' ') + 1);
					if(n.startsWith(name)){
						return i;
					}
				} catch (Throwable t){
					//there was some funny syntax
					t.printStackTrace();
					System.err.println("[" + getDescription().getName() + "] File syntax error in id file. Skipping...");
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return -1;

	}

	public File getDataFile(String name, boolean download){
		File f = new File(getDataFolder() + File.separator + name);
		if(f.exists())
			return f;
		try {
			f.getParentFile().mkdirs();
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if(download){
			try {
				URL url = new URL(fileRepository + name);
				BufferedInputStream in = new BufferedInputStream(url.openStream());
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
				int i;
				while((i = in.read()) != -1){
					out.write(i);
				}
				in.close();
				out.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return f;
	}

	private void loadDamageMap(){
		ConfigurationSection section = getConfig().getConfigurationSection("damages");
		for(String s : section.getKeys(false)){
			if(s.equals("priority") || s.equals("expression"))
				continue;
			ConfigurationSection sub = section.getConfigurationSection(s);
			World w = (s.equals("default")) ? null : getServer().getWorld(s);
			System.out.println("[" + getDescription().getName() + "] Loading damages for world " + ((w == null) ? "default" : w.getName()));
			for(String str : sub.getKeys(false)){
				try{
					if(verbose){
						System.out.println("[ChangeDamage] Loading " + str + " with value " + sub.getString(str));
					}
					int i;
					if((i = Arrays.binarySearch(DamageListener.PROJECTILE_TYPES, str.toLowerCase())) >= 0){
						dl.setProjectileDamage(w, DamageListener.PROJECTILE_TYPES[i], sub.getDouble(str));
						if(verbose){
							System.out.println("[ChangeDamage] Loaded as projectile");
						}
					} else if (str.equalsIgnoreCase("default")){
						dl.setDefaultDamage(w, sub.getInt(str));
						if(verbose){
							System.out.println("[ChangeDamage] Loaded as default damage");
						}
					} else {
						try{
							i = Integer.parseInt(str);
						} catch (NumberFormatException ex){
							i = Material.getMaterial(str).getId();
						}
						dl.put(w, i, sub.getInt(str));
						if(verbose){
							System.out.println("[ChangeDamage] Loaded as item");
						}
					}
				} catch (Exception ex){
					ex.printStackTrace();
					System.out.println("[" + getDescription().getName() + "]Configuration node damage." + s + "." + str + " is causing an issue.");
				}
			}
		}

		dl.setEventPriority(getConfig().getString("damages.priority", "NORMAL"));

		System.out.println("[" + getDescription().getName() + "]Successfully loaded damages!");
	}


	//removed due to Bukkit Dev safety rules
	/*private boolean update(){
		String mode = getConfig().getString("update.mode").toLowerCase();
		String baseURL;
		if(mode.equals("experimental"))
			baseURL = experimentalUpdateRepository;
		else if (mode.equals("stable"))
			baseURL = stableUpdateRepository;
		else return false;

		System.out.println("[ChangeDamage] WARNING! ENTERING EXPERIMENTAL AUTOUPDATE MODE. THIS MAY CORRUPT YOUR VERSION OF CHANGEDAMAGE. TO DISABLE THIS, CHANGE update.mode IN CONFIG.YML TO off. PRESS CTRL + C NOW TO FORCE-STOP BUKKIT AND PREVENT THE UPDATE.");
		System.out.println("[ChangeDamage] WARNING! UPDATE COMMENCING IN 10 SECONDS!!!");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String tempLoc = getDataFolder() + File.separator + "temp.tmp";
		String downloadLoc = getFile().toString();
		String version = getDescription().getVersion();
		try{
			Updater.update(baseURL + updateJAR, baseURL + updateVersion, downloadLoc, tempLoc, version);
			System.out.println("Successfully updated! Reloading...");
		} catch (Exception ex){
			System.out.println("Update failed!");
			ex.printStackTrace();
		}
		Bukkit.reload();
		return true;

	}*/

	@Override
	public void onDisable(){
		armorHook.restore();
	}

}
