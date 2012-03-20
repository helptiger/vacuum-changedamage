package vacuum.changedamage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ChangeDamage extends JavaPlugin{

	private DamageListener dl;
	private boolean verbose;
	private static final String fileRepository = "http://vacuum-changedamage.googlecode.com/svn/trunk/resources/";
	private static final String damageFile = "damages.txt";
	private static final String idFile = "items.txt";
	
	@Override
	public void onEnable() {
		getDataFile("config.yml", false);
		if(!getConfig().contains("pvponly")){
			getConfig().createSection("pvponly");
			getConfig().set("pvponly", true);
		}
		if(!getConfig().contains("verbose")){
			getConfig().createSection("verbose");
			getConfig().set("verbose", false);
		}

		if(!getConfig().contains("defaultdamage")){
			getConfig().createSection("defaultdamage");
			getConfig().set("defaultdamage", -1);
		}
		verbose = getConfig().getBoolean("verbose", false);
		dl = new DamageListener();
		loadDamageMap();
		getServer().getPluginManager().registerEvents(dl, this);
	}

	private void loadDamageMap() {
		dl.clear();
		File f = getDataFile(damageFile, true);
		try {
			Scanner s = new Scanner(f);
			while(s.hasNext()){
				String line = s.nextLine();
				try {
					String name = line.substring(0, line.indexOf(' ')).toUpperCase().replace(" ", "_");
					if(name.equals("FLYING_ARROW")){
						dl.setArrowDamage(Double.parseDouble(line.substring(line.indexOf(' ') + 1)));
						continue;
					}
					int id = getID(name);
					if(id == -1){
						System.out.println("Failed to find item " + name);
						continue;
					}
					int damage = Integer.parseInt(line.substring(line.indexOf(' ') + 1));
					dl.put(id, damage);
					if(verbose)
					System.out.println("Put " + id + ", " + damage);
				} catch (Throwable t){
					//there was some funny syntax
					t.printStackTrace();
					System.err.println("[" + getDescription().getName() + "] File syntax error in id file. Skipping...");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		dl.setPVPOnly(getConfig().getBoolean("pvponly", true));
		dl.setVerbose(verbose);
		dl.setDefaultDamage(getConfig().getInt("defaultdamage", -1));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(command.getName().equalsIgnoreCase("changedamage")){
			if(args[0].equalsIgnoreCase("release")){
				this.getPluginLoader().disablePlugin(this);
			} else if(args[0].equalsIgnoreCase("reload")){
				loadDamageMap();
			} else
				return false;
			return true;
		}
		return false;
	}
	
	public int getID(String name){
		
		try{
			return Integer.parseInt(name);
		} catch (NumberFormatException ex){
			//ignore
		}
		File f = getDataFile(idFile, true);
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
	
}
