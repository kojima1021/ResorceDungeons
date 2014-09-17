package com.thekarura.bukkit.plugin.resorcedungons.listener;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.thekarura.bukkit.plugin.resorcedungons.ResorceDungeons;
import com.thekarura.bukkit.plugin.resorcedungons.manager.DungeonMossy;

/**
 * ResorceDungeonsのブロック関連を扱う部分です。
 *
 * @author karura
 */
public class RDBlockListener implements Listener {

	// ++ Logger ++ //
	public static final Logger log = ResorceDungeons.log;
	private static final String logPrefix = ResorceDungeons.logPrefix;
	private static final String msgPrefix = ResorceDungeons.msgPrefix;

	// ++ Instance ++ //
	private ResorceDungeons instance = ResorceDungeons.getInstance();

	// ++ Constructor ++ //
	public RDBlockListener(final ResorceDungeons plugin) {
		this.instance = plugin;
	}

	/**
	 * プレイヤーがサーバーに参加したときに発生するイベント
	 * @param event
	 */
	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		// 参加したプレイヤーに、1秒毎に動作する非同期処理タイマーを仕掛ける。
		final Player player = event.getPlayer();
		
		new BukkitRunnable() {
			
			// 1秒前に居たプレイヤーの位置 の保存用変数
			private Location prev;
			
			//感知範囲(コンフィグで調整予定)
			final int radius = 50;
			
			@Override
			public void run() {
				
				// もしプレイヤーがサーバーから抜けたなら、このタイマーを終了する
				if ( !player.isOnline() ) {
					cancel();
					return;
				}
				
				// プレイヤーがチェック対象ワールドにいないなら処理をしない
				if ( !player.getWorld().getName().equals(instance.getConfigs().getDungeonWorld())) {
					prev = null;
					return;
				}
				
				Location loc = player.getLocation();
				if ( prev != null ) {
					// 1秒前の位置キャッシュがある場合
					
					// x方向に移動した場合、"移動した分のブロックだけ"をチェックする
					if ( loc.getBlockX() != prev.getBlockX() ) {
						Location min = (loc.getBlockX() < prev.getBlockX() ) ? loc.clone() : prev.clone();
						Location max = (loc.getBlockX() < prev.getBlockX() ) ? prev.clone() : loc.clone();
						
						// 調査範囲は、min{x, y-50, z-50} から max{x, y+50, z+50}
						min.add(0, -radius, -radius);
						max.add(0, radius, radius);
						
						// サーチ
						search(min, max);
					}
					
					// y方向に移動した場合も同様。
					if ( loc.getBlockY() != prev.getBlockY() ) {
						Location min = (loc.getBlockY() < prev.getBlockY() ) ? loc.clone() : prev.clone();
						Location max = (loc.getBlockY() < prev.getBlockY() ) ? prev.clone() : loc.clone();
						
						// 調査範囲は、min{x-50, y, z-50} から max{x+50, y, z+50}
						min.add(-radius, 0, -radius);
						max.add(radius, 0, radius);
						
						// サーチ
						search(min, max);
					}
					
					// z方向に移動した場合も同様。
					if ( loc.getBlockZ() != prev.getBlockZ() ) {
						Location min = (loc.getBlockZ() < prev.getBlockZ() ) ? loc.clone() : prev.clone();
						Location max = (loc.getBlockZ() < prev.getBlockZ() ) ? prev.clone() : loc.clone();
						
						// 調査範囲は、min{x-50, y-50, z} から max{x+50, y+50, z}
						min.add(-radius, -radius, 0);
						max.add(radius, radius, 0);
						
						// サーチ
						search(min, max);
					}
					
				} else {
					// 1秒前の位置キャッシュが無い場合、しかたがないので全範囲をサーチする
					Location min = loc.clone().add(-radius, -radius, -radius);
					Location max = loc.clone().add(radius, radius, radius);
					search(min, max);
					
				}
				
				// 今回のサーチした位置を保存しておく
				prev = loc;
			}
			
			private void search(Location min, Location max) {
				
				//ループ処理で特定のブロックを探します
				World world = min.getWorld();
				for (int x = min.getBlockX(); x < max.getBlockX(); x++ ){
					for (int y = min.getBlockY(); y < min.getBlockY(); y++ ){
						for (int z = min.getBlockZ(); z < min.getBlockZ(); z++ ){
							
							/* コマンドブロックの上に立っている看板を探します
							 * 図式
							 *
							 * [POST]
							 * [COMB]
							 *
							 */
							if(world.getBlockAt(x, y, z).getType() == Material.COMMAND
							&& world.getBlockAt(x, y+1, z).getType() == Material.SIGN_POST){
								
								//看板の情報を上記のSIGN_POSTに設定します。
								Sign sign = (Sign) world.getBlockAt(x, y+1, z).getState();
								
								//生成位置を決めるため位置情報を登録します。
								Location loc = new Location(world, x, y, z);
								
								//[RDungeons]と書かれている場合の条件
								if(sign.getLine(0).equals("[RDungeons]")){
									
									//MossyDungeonsを生成します
									if (sign.getLine(1).equals("Mossy")){
										
										remove(world, x, y, z);
										new DungeonMossy().setDungeonMossy(loc);
									}
								}
							}
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(ResorceDungeons.getInstance(), 20, 20);
		// ↑タスクを非同期で、20ticks(1秒)後から、20ticksごとにrunメソッドを実行する
	}

	/**
	 * 看板削除用メソッド
	 * @param w
	 * @param i
	 * @param y
	 * @param j
	 */
	private void remove(World w ,int i ,int y ,int j){
		for (int y_ = 2 ; y_ != -1 ; y_-- ){
			w.getBlockAt(i, y + y_, j).setType(Material.AIR);
		}
	}

}
