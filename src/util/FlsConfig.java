package util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.ReferralCode;

import connect.Connect;

public class FlsConfig extends Connect{

	//This is the build of the app, hardcoded here.
	//Increase it on every change that needs a upgrade hook
	public final int appBuild = 2007;			

	public static int dbBuild = 0;		//This holds the build of the db, got from the database
	public static String env = null;	//This holds the env, got from the db
	
	
	String getEnv() {

		//TODO: select value from config where option = "env"
		try {
			getConnection();
			String sql = null;
			PreparedStatement sql_stmt = null;
			sql = "SELECT value FROM `config` WHERE config.option='env'";
			sql_stmt = connection.prepareStatement(sql);
			
			ResultSet dbResponse = sql_stmt.executeQuery();
			
			if(dbResponse.next()){
				env = dbResponse.getString("value");
			}
			sql_stmt.close();

		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				// close and reset connection to null
				connection.close();
				connection = null;
			} catch (SQLException e){
				e.printStackTrace();
			}
		}
		
		if(env == null){
			System.out.println("env variable is null");
		}else{
			System.out.println(env);
		}

		//env return
		return env;
	}
	
	public boolean setEnv () {
		
		env = getEnv();
		
		if (env !=null){
			return true;
		}else{
			return false;
		}	
	}
	
	int getDbBuild() {
		
		try {
			getConnection();
			String build = null;
			String sql = null;
			PreparedStatement sql_stmt = null;
			sql = "SELECT value FROM `config` WHERE config.option='build'";
			sql_stmt = connection.prepareStatement(sql);
			
			ResultSet dbResponse = sql_stmt.executeQuery();
			
			if(dbResponse.next()){
				build = dbResponse.getString("value");
				dbBuild = Integer.parseInt(build);
			}
			sql_stmt.close();
			
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				// close and reset connection to null
				connection.close();
				connection = null;
			} catch (SQLException e){
				e.printStackTrace();
			}
		} 
			
		return dbBuild;
	}
	
	public void setDbBuild() {
		
		dbBuild = getDbBuild();
		
		if(dbBuild== 0){
			System.out.println("DB BUILD is 0");
			return;
		}
		
		if(dbBuild == appBuild){
			System.out.println("dbBuild in sync");
		}
		
		if (dbBuild < 2001) {
			//do new things - noop for this build
			
			//TODO: update config set value = 2001 where option = "build"
			dbBuild = 2001;
		}
		// add new build hooks one after the other in increasing order
		if (dbBuild < 2002) {
			
			// New column created to store the uid of the item
			String sqlAddUid = "ALTER TABLE `items` ADD `item_uid` VARCHAR(255) NULL DEFAULT NULL AFTER `item_status`";
			try {
				getConnection();
				PreparedStatement ps1 = connection.prepareStatement(sqlAddUid);
				ps1.executeUpdate();
				ps1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println(e.getStackTrace());
				}
			}
			
			// These queries are updating items table to add item_uid
			String getAllItemIdAndItemName = "SELECT item_id, item_name FROM `items`";
			try{
				getConnection();
				PreparedStatement ps2 = connection.prepareStatement(getAllItemIdAndItemName);
				ResultSet rs = ps2.executeQuery();
				
				while(rs.next()){
					String uid = rs.getString("item_name") + " " + rs.getInt("item_id");
					uid = uid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
					
					String sqlUpdateRowUID = "UPDATE items SET item_uid=? WHERE item_id=?";
					PreparedStatement s = connection.prepareStatement(sqlUpdateRowUID);
					s.setString(1, uid);
					s.setInt(2, rs.getInt("item_id"));
					s.executeUpdate();
				}
				
				ps2.close();
			} catch(SQLException e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println(e.getStackTrace());
				}
			}
			
			// The dbBuild version value is changed in the database
			dbBuild = 2002;
			updateDBBuild(dbBuild);
		}
		
		if(dbBuild < 2003){
			String sqlAddUserCredit = "ALTER TABLE `users` ADD `user_credit` INT(255) NOT NULL DEFAULT '10' AFTER `user_status`";
			try {
				getConnection();
				PreparedStatement ps1 = connection.prepareStatement(sqlAddUserCredit);
				ps1.executeUpdate();
				ps1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println(e.getStackTrace());
				}
			}
			
			dbBuild = 2003;
			updateDBBuild(dbBuild);
		}
		
		if(dbBuild < 2004){
			String sqlAddLeasePrimaryKey = "ALTER TABLE leases DROP PRIMARY KEY, ADD PRIMARY KEY(lease_id, lease_requser_id, lease_item_id);";
			try {
				getConnection();
				PreparedStatement ps1 = connection.prepareStatement(sqlAddLeasePrimaryKey);
				ps1.executeUpdate();
				ps1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println(e.getStackTrace());
				}
			}
			
			dbBuild = 2004;
			updateDBBuild(dbBuild);
		}
		
		if(dbBuild < 2005){
			// updating users table for location data
			String sql_users_location_columns = "ALTER TABLE `users` ADD `user_lat` FLOAT(10,6) NOT NULL AFTER `user_credit`, ADD `user_lng` FLOAT(10,6) NOT NULL AFTER `user_lat`, ADD `user_address` VARCHAR(255) NOT NULL AFTER `user_lng`, ADD `user_locality` VARCHAR(255) NOT NULL AFTER `user_address`, ADD `user_sublocality` VARCHAR(255) NOT NULL AFTER `user_locality`";
			try{
				getConnection();
				PreparedStatement ps = connection.prepareStatement(sql_users_location_columns);
				ps.executeUpdate();
				ps.close();
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}finally{
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println(e.getStackTrace());
				}
			}
			
			// updating items table for location data
			String sql_items_location_columns = "ALTER TABLE `items` ADD `item_lat` FLOAT(10,6) NOT NULL AFTER `item_uid`, ADD `item_lng` FLOAT(10,6) NOT NULL AFTER `item_lat`";
			try{
				getConnection();
				PreparedStatement ps = connection.prepareStatement(sql_items_location_columns);
				ps.executeUpdate();
				ps.close();
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}finally{
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println(e.getStackTrace());
				}
			}
			
			dbBuild = 2005;
			updateDBBuild(dbBuild);
		}
		
		
		if(dbBuild < 2006){
			// updating items table for location data
			String sql_edit_location_columns = "ALTER TABLE items MODIFY COLUMN item_lat FLOAT(10,6), MODIFY COLUMN item_lng FLOAT(10,6);";
			try{
				getConnection();
				PreparedStatement ps = connection.prepareStatement(sql_edit_location_columns);
				ps.executeUpdate();
				ps.close();
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}finally{
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (SQLException e){
					e.printStackTrace();
					System.out.println(e.getStackTrace());
				}
			}
			
			dbBuild = 2006;
			updateDBBuild(dbBuild);
		}
		
			// This block adds referral code to users table
				if (dbBuild < 2007) {
					
					// New column created to store the uid of the item
					String sqlAddRefCode = "ALTER TABLE `users` ADD `user_referral_code` VARCHAR(255) NULL DEFAULT NULL AFTER `user_sublocality`, ADD `user_referrer_code` VARCHAR(255) NULL DEFAULT NULL AFTER `user_referral_code`";
					try {
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddRefCode);
						ps1.executeUpdate();
						ps1.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					} finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (SQLException e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					
					// These queries are updating items table to add item_uid
					String getAllUserId = "SELECT user_id FROM `users`";
					try{
						getConnection();
						PreparedStatement ps2 = connection.prepareStatement(getAllUserId);
						ResultSet rs = ps2.executeQuery();
						
						while(rs.next()){
							String ref_code = rs.getString("user_id");
							int ref_code_length = 8;
							ReferralCode rc = new ReferralCode();
							ref_code = rc.createRandomCode(ref_code_length, ref_code);
							
							String sqlUpdateRowReferralCode = "UPDATE users SET user_referral_code=? WHERE user_id=?";
							PreparedStatement s = connection.prepareStatement(sqlUpdateRowReferralCode);
							s.setString(1, ref_code);
							s.setString(2, rs.getString("user_id"));
							s.executeUpdate();
							s.close();
						}
						rs.close();
						ps2.close();
						System.out.println("REF_Codes Added");
					} catch(SQLException e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					} finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (SQLException e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2007;
					updateDBBuild(dbBuild);
				}
	}
	
	private void updateDBBuild(int version){
		
		String sqlUpdateDBBuild = "UPDATE config set `value` = "+ version +" where `option` = 'build'";
		try{
			getConnection();
			System.out.println("Before Updating DBBUILD");
			PreparedStatement ps = connection.prepareStatement(sqlUpdateDBBuild);
			ps.executeUpdate();
			ps.close();
			
			System.out.println("dbBuild updated to "+version);
		} catch(SQLException e){
			e.printStackTrace();
			System.out.println(e.getStackTrace());
		} finally {
			try {
				// close and reset connection to null
				connection.close();
				connection = null;
			} catch (SQLException e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}
		}
	}
	
}
