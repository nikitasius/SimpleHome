/*
 * Copyright (C) 2011 <silence@immortal-forces.net>
 *
 * This file is part of the Bukkit plugin Whitelist.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package net.immortal_forces.silence.plugin.simplehome;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class SimpleHome extends JavaPlugin
{
  private final String FILE_HOMES = "homes.txt";

  private File m_Folder;
  private HashMap<String, Location> m_Homes = new HashMap<String, Location>();
  
  public SimpleHome()
  {
    super();
  }

  public void onEnable() 
  {
    //From Ctor
    m_Folder = getDataFolder();
    
    // Register our events
    PluginManager pm = getServer().getPluginManager();
    
    //Create folders and files
    if ( !m_Folder.exists() )
    {
      System.out.print("SimpleHome: Config folder missing, creating...");
      m_Folder.mkdir();
      System.out.println("done.");
    }
    File homelist = new File(m_Folder.getAbsolutePath() + File.separator + FILE_HOMES);
    if ( !homelist.exists() )
    {
      System.out.print("SimpleHome: Homelist is missing, creating...");
      try
      {
        homelist.createNewFile();
        System.out.println("done.");
      }
      catch ( IOException ex )
      {
        System.out.println("failed.");
      }
    }

    System.out.print("SimpleHome: Loading homelist...");
    if ( loadSettings() )
      System.out.println("done.");
    else
      System.out.println("failed.");
    
    PluginDescriptionFile pdfFile = this.getDescription();
    System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
  }

  public void onDisable()
  {
    System.out.println("Goodbye world!");
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  {
    Player player = null;
    try
    {
      player = (Player)sender;
    }
    catch (Exception e)
    {
      sender.sendMessage(ChatColor.RED + "Only players can use this command!");
      return true;
    }

    if ( cmd.getName().compareToIgnoreCase("home") == 0 )
    {
      Location loc = m_Homes.get(player.getName());
      if ( loc != null )
      {
        player.teleportTo(loc);
      }
      else
      {
        player.sendMessage( ChatColor.RED + "Home not set, yet. Set it with /home, first" );
      }
      return true;
    }
    if ( cmd.getName().compareToIgnoreCase("sethome") == 0 )
    {
      m_Homes.put(player.getName(), player.getLocation());
      player.sendMessage( ChatColor.GREEN + "Home set!" );
      saveSettings();
      return true;
    }
    return false;
  }

  private boolean saveSettings()
  {
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter((m_Folder.getAbsolutePath() + File.separator + FILE_HOMES)));
      for ( Entry<String,Location> entry : m_Homes.entrySet() )
      {
        Location loc = entry.getValue();
        if ( loc != null )
        {
          writer.write(entry.getKey() + ";" + loc.getX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ() + ";" + loc.getPitch() + ";" + loc.getYaw() + ";" + loc.getWorld().getName() );
          writer.newLine();
        }
      }
      writer.close();
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public boolean loadSettings()
  {
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader((m_Folder.getAbsolutePath() + File.separator + "homes.txt")));
      String line = reader.readLine();
      while ( line != null )
      {
        String[] values = line.split(";");
        if ( values.length == 7 )
        {
          double X = Double.parseDouble(values[1]);
          double Y = Double.parseDouble(values[2]);
          double Z = Double.parseDouble(values[3]);
          float pitch = Float.parseFloat(values[4]);
          float yaw = Float.parseFloat(values[5]);

          World world = getServer().getWorld(values[6]);
          if ( world != null )
            m_Homes.put(values[0], new Location(world, X, Y, Z, yaw, pitch));
        }
        line = reader.readLine();
      }
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }
}
