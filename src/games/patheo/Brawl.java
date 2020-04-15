package games.patheo;

import games.patheo.scoreboard.ScoreHelper;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Brawl extends JavaPlugin implements Listener {

    private File statsConfigFile;
    private FileConfiguration statsConfig;

    Player player;

    @Override
    public void onEnable() {
        createStatsConfig();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveStatsConfig();
    }

    private void createScoreboard(Player player) {
        ScoreHelper helper = ScoreHelper.createScore(player);
        helper.setTitle("&e&lFFA");
        helper.setSlot(8, "&r");
        helper.setSlot(7, "&f&lKills");
        helper.setSlot(6, "&7» " + getStatsConfig().getInt(player.getUniqueId() + ".kills"));
        helper.setSlot(5, "&r");
        helper.setSlot(4, "&f&lDeaths");
        helper.setSlot(3, "&7» " + getStatsConfig().getInt(player.getUniqueId() + ".deaths"));
        helper.setSlot(2, "&r");
        helper.setSlot(1, "&eplay.mineset.net");
    }

    private void updateScoreboard(Player player) {
        if (ScoreHelper.hasScore(player)) {
            ScoreHelper helper = ScoreHelper.getByPlayer(player);
            helper.setSlot(6, "&7» " + getStatsConfig().getInt(player.getUniqueId() + ".kills"));
            helper.setSlot(3, "&7» " + getStatsConfig().getInt(player.getUniqueId() + ".deaths"));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createScoreboard(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (ScoreHelper.hasScore(player)) {
            ScoreHelper.removeScore(player);
        }
    }

    public FileConfiguration getStatsConfig() {
        return this.statsConfig;
    }

    public void saveStatsConfig() {
        try {
            statsConfig.save(statsConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createStatsConfig() {
        statsConfigFile = new File(getDataFolder(), "stats.yml");
        if (!statsConfigFile.exists()) {
            statsConfigFile.getParentFile().mkdirs();
            saveResource("stats.yml", false);
        }

        statsConfig= new YamlConfiguration();
        try {
            statsConfig.load(statsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void createStats(Player p) {
        if(getStatsConfig().get(p.getUniqueId().toString()) == null) {
            getStatsConfig().set(p.getUniqueId().toString() + ".kills", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".deaths", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".level", 0);
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", 0);
            saveStatsConfig();
            updateScoreboard(p);
        }
    }

    public void addKill(Player p) {
        getStatsConfig().set(p.getUniqueId().toString() + ".kills", getStatsConfig().getInt(p.getUniqueId().toString() + ".kills") + 1);
        saveStatsConfig();
        updateScoreboard(p);
    }

    public void addDeath(Player p) {
        getStatsConfig().set(p.getUniqueId().toString() + ".deaths", getStatsConfig().getInt(p.getUniqueId().toString() + ".deaths") + 1);
        saveStatsConfig();
        updateScoreboard(p);
    }

    public void addXP(Player p) {
        Random r = new Random();
        int low = 2;
        int high = 12;
        int result = r.nextInt(high-low) + low;
        if (getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") < 45) {
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result);
            saveStatsConfig();
            p.sendMessage("§d+ " + result + " XP");
        } else if ((getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result) < 50) {
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result);
            saveStatsConfig();
            p.sendMessage("§d+ " + result + " XP");
        } else {
            getStatsConfig().set(p.getUniqueId().toString() + ".XP", (getStatsConfig().getInt(p.getUniqueId().toString() + ".XP") + result) - 50);
            getStatsConfig().set(p.getUniqueId().toString() + ".level", getStatsConfig().getInt(p.getUniqueId().toString() + ".level") + 1);

            saveStatsConfig();

            Bukkit.getServer().broadcastMessage("§6" + p.getName() + " has leveled up to level " + getStatsConfig().getInt(p.getUniqueId().toString() + ".level"));
            p.sendMessage("§d+ " + result + " XP");
        }
    }

    public void level0(Player p) {
        p.getInventory().clear();

        p.setGameMode(GameMode.ADVENTURE);
        p.setMaxHealth(20);
        p.setFoodLevel(20);
        p.setHealth(20);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 0));

        p.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD) {{
            ItemMeta meta = getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
            setItemMeta(meta);
        }});
        p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE) {{
            ItemMeta meta = getItemMeta();
            setItemMeta(meta);
        }});
        p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS) {{
            ItemMeta meta = getItemMeta();
            setItemMeta(meta);
        }});
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS) {{
            ItemMeta meta = getItemMeta();
            setItemMeta(meta);
        }});
    }

    public void level5(Player p) {
        p.getInventory().clear();

        p.setGameMode(GameMode.ADVENTURE);
        p.setMaxHealth(20);
        p.setFoodLevel(20);
        p.setHealth(20);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 0));

        p.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD) {{
            ItemMeta meta = getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
            setItemMeta(meta);
        }});
        p.getInventory().setItem(1, new ItemStack(Material.FISHING_ROD) {{
            ItemMeta meta = getItemMeta();
            meta.addEnchant(Enchantment.DURABILITY, 10, true);
            setItemMeta(meta);
        }});
        p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE) {{
            ItemMeta meta = getItemMeta();
            setItemMeta(meta);
        }});
        p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS) {{
            ItemMeta meta = getItemMeta();
            setItemMeta(meta);
        }});
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS) {{
            ItemMeta meta = getItemMeta();
            setItemMeta(meta);
        }});
    }

    @EventHandler
    public void onPlayerDrop(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        World world = Bukkit.getServer().getWorld("Lobby");
        List<Entity> entList = world.getEntities();

        for (Entity current : entList) {
            if (current instanceof Item) {
                current.remove();
            }
        }

        if (loc.getBlockY() == 90) {
            level0(p);
            //TitleAPI.sendTitle(p, 30, 60, 30, "§e§lFIGHT");
            if (getStatsConfig().getInt(p.getUniqueId().toString() + ".level") == 5) {
                level5(p);
                p.sendMessage("level 2 working");
                //TitleAPI.sendTitle(p, 30, 60, 30, "§e§lFIGHT");
            } else {
                return;
            }
        }
    }
}
