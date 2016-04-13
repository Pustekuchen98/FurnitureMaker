package de.Ste3et_C0st.DiceFurnitureMaker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.Ste3et_C0st.FurnitureLib.NBT.CraftItemStack;
import de.Ste3et_C0st.FurnitureLib.NBT.NBTCompressedStreamTools;
import de.Ste3et_C0st.FurnitureLib.NBT.NBTTagCompound;
import de.Ste3et_C0st.FurnitureLib.main.ObjectID;

public class ProjektInventory implements Listener{

	private Inventory inv;
	private Player player;
	private ObjectID id;
	
	public ProjektInventory(int slots, ObjectID id){
		inv = Bukkit.createInventory(null, slots, id.getProjectOBJ().getCraftingFile().getRecipe().getResult().getItemMeta().getDisplayName());
		this.id = id;
		Bukkit.getPluginManager().registerEvents(this, main.getInstance());
	}
	
	public Player getPlayer() {
		return player;
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e){
		if(getPlayer()==null) return;
		if(getInv()==null) return;
		if(e.getInventory().equals(inv)){
			if(e.getPlayer().equals(getPlayer())){
				this.player = null;
				config c = new config();
				FileConfiguration file = c.getConfig(this.id.getSerial(), "metadata/");
				file.set("inventory", toString());
				c.saveConfig(this.id.getSerial(), file, "metadata/");
			}
		}
	}
	
	public void openInventory(Player player) {
		if(this.inv==null) return;
		this.player = player;
		getPlayer().openInventory(inv);
	}
	
	public Inventory getInv() {
		return inv;
	}
	
	public void load(){
		config c = new config();
		FileConfiguration file = c.getConfig(this.id.getSerial(), "metadata/");
		if(file.isSet("inventory")){
			setItems(file.getString("inventory"));
		}
	}
	
	public String toString(){
		if(inv==null) return "";
		NBTTagCompound inventory = new NBTTagCompound();
		NBTTagCompound items = new NBTTagCompound();
		for(int i = 0; i<inv.getContents().length;i++ ){
			if(inv.getContents()[i]==null||inv.getContents()[i].getType()==null||inv.getContents()[i].getType().equals(Material.AIR)){
				items.setString(i+"", "NONE");continue;
			}
			try {
				items.set(i+"", new CraftItemStack().getNBTTag(inv.getContents()[i]));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		inventory.setInt("size", getInv().getSize());
		inventory.set("inventory", items);
		return Base64.encodeBase64String(armorStandtoBytes(inventory));
	}
	
	public void setItems(String string){
		byte[] by = Base64.decodeBase64(string);
		ByteArrayInputStream bin = new ByteArrayInputStream(by);
		try {
			NBTTagCompound compound = NBTCompressedStreamTools.read(bin);
			NBTTagCompound inventory = compound.getCompound("inventory");
			int size = compound.getInt("size");
			this.inv = Bukkit.createInventory(null, size, id.getProjectOBJ().getCraftingFile().getRecipe().getResult().getItemMeta().getDisplayName());
			for(int i = 0; i<size;i++){
				if(!inventory.getString(i+"").equalsIgnoreCase("NONE")){
					ItemStack is = new CraftItemStack().getItemStack(inventory.getCompound(i+""));
					this.inv.setItem(i, is);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private byte[] armorStandtoBytes(NBTTagCompound compound) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			NBTCompressedStreamTools.write(compound, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[0];
		}
		return out.toByteArray();
	}
}
