package util;

import java.security.Key;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.ReferralCode;
import util.FlsS3Bucket.Bucket_Name;
import util.FlsS3Bucket.File_Name;
import util.FlsS3Bucket.Path_Name;
import connect.Connect;
import io.jsonwebtoken.impl.crypto.MacProvider;

public class FlsConfig extends Connect{

	//This is the build of the app, hardcoded here.
	//Increase it on every change that needs a upgrade hook

	public final int appBuild = 2037;

	public static int dbBuild = 0;		//This holds the build of the db, got from the database
	public static String env = null;	//This holds the env, got from the db
	
	public static String prefixUrl = "http://www.frrndlease.com";
	
	String getEnv() {

		//select value from config where option = "env"
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

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// close and reset connection to null
				connection.close();
				connection = null;
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		if(env == null){
			System.out.println("env variable is null");
		}else{
			System.out.println(env);
		}

		if(env.equals("dev"))
			prefixUrl = "http://localhost:8080/flsv2";
		
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
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// close and reset connection to null
				connection.close();
				connection = null;
			} catch (Exception e){
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
			
			//update config set value = 2001 where option = "build"
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
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (Exception e){
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
			} catch(Exception e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (Exception e){
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
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (Exception e){
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
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			} finally {
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (Exception e){
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
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}finally{
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (Exception e){
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
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}finally{
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (Exception e){
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
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}finally{
				try {
					// close and reset connection to null
					connection.close();
					connection = null;
				} catch (Exception e){
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
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					} finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					
					// These queries are updating items table to add referral code
					String getAllUserId = "SELECT user_id FROM `users`";
					try{
						getConnection();
						PreparedStatement ps2 = connection.prepareStatement(getAllUserId);
						ResultSet rs = ps2.executeQuery();
						
						while(rs.next()){
							String ref_code = rs.getString("user_id");
							int ref_code_length = 8;
							ReferralCode rc = new ReferralCode();
							ref_code = rc.createRandomCode(ref_code_length);
							
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
					} catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					} finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2007;
					updateDBBuild(dbBuild);
				}
				
				
				// This block adds user photo id to users table
				if (dbBuild < 2008) {
					
					// New column created to store the uid of the item
					String sqlAddPhotoId = "ALTER TABLE `users` ADD `user_photo_id` MEDIUMTEXT NULL DEFAULT NULL AFTER `user_referrer_code`, ADD `user_verified_flag` BOOLEAN NOT NULL DEFAULT FALSE AFTER `user_photo_id`";
					try {
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddPhotoId);
						ps1.executeUpdate();
						ps1.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					} finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
							
					// The dbBuild version value is changed in the database
					dbBuild = 2008;
					updateDBBuild(dbBuild);
				}
				
				// This block adds user photo id to users table
				if (dbBuild < 2009) {
					
					// New table credit_log added to the database
					String sqlAddPhotoId = "CREATE TABLE credit_log (credit_log_id int(11) NOT NULL AUTO_INCREMENT,credit_user_id varchar(255), credit_date DATETIME, credit_amount int(11), credit_type varchar(255), credit_desc varchar(255),  primary key (credit_log_id))";
					try {
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddPhotoId);
						ps1.executeUpdate();
						ps1.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					} finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
							
					// The dbBuild version value is changed in the database
					dbBuild = 2009;
					updateDBBuild(dbBuild);
				}
				
				//  This block adds admin and ops user id to the users table
				if(dbBuild < 2010){
					
					// inserting two user ids each for admin and ops
					String sqlAddAdminOps = "INSERT INTO `users` (`user_id`, `user_full_name`, `user_mobile`, `user_location`, `user_auth`, `user_activation`, `user_status`, `user_credit`, `user_lat`, `user_lng`, `user_address`, `user_locality`, `user_sublocality`) VALUES ('admin@frrndlease.com', 'Admin', '1234567890', 'None', '859e4768db04cba0422771ea97a44dbb', 'None', 'email_activated', '10', 0.0, 0.0, '', '', ''), ('ops@frrndlease.com', 'Ops', '1234567890', 'None', '859e4768db04cba0422771ea97a44dbb', 'None', 'email_activated', '10', 0.0, 0.0, '', '', '')";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddAdminOps);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2010;
					updateDBBuild(dbBuild);
					
				}
				
				// This block adds promo code table for credits
				if(dbBuild < 2011){
					
					// creating a promo code table for credits
					String sqlAddPromoTable = "CREATE TABLE `fls`.`promo_credits` ( `id` INT(255) NOT NULL AUTO_INCREMENT , `code` VARCHAR(255) NULL , `credit` INT(255) NULL , `expiry` DATE NULL , PRIMARY KEY (`id`))";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddPromoTable);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2011;
					updateDBBuild(dbBuild);
				}
				
				// This block adds Profile Picture to users table
				if (dbBuild < 2012) {
					
					// New column created to store the uid of the item
					String sqlAddPhotoId = "ALTER TABLE `users` ADD `user_profile_picture` varchar(255) NULL DEFAULT NULL AFTER `user_verified_flag`";
					try {
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddPhotoId);
						ps1.executeUpdate();
						ps1.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					} finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2012;
					updateDBBuild(dbBuild);
				}

				//This block adds item timeline table
				if(dbBuild < 2013){
					
					// creating a item log table
					String sqlItemLogTable = "CREATE TABLE `fls`.`item_log` ( `item_log_id` INT(11) NOT NULL AUTO_INCREMENT , `item_id` INT(255) NULL , `item_log_date` DATETIME NULL , `item_log_type` VARCHAR(255) NULL , `item_log_desc` VARCHAR(255) NULL , `item_log_image` MEDIUMTEXT NULL DEFAULT NULL , PRIMARY KEY (`item_log_id`))";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlItemLogTable);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e.getStackTrace());
						}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2013;
					updateDBBuild(dbBuild);
				}
				
				//This block adds user_live_status column in users table
				if(dbBuild < 2014){
					
					// creating a item log table
					String sqlLiveStatusColumn = "ALTER TABLE `users` ADD `user_live_status` TINYINT(1) NOT NULL DEFAULT '1' AFTER `user_profile_picture`";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlLiveStatusColumn);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2014;
					updateDBBuild(dbBuild);
				}
				
				//This block adds api_key column in config table
				if(dbBuild < 2015){
					
					Key key = MacProvider.generateKey();
					
					// inserting api_key for OAuth in APIs
					String sqlInsertApiKey = "INSERT INTO `config`(`option`, `value`) VALUES (?,?)";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlInsertApiKey);
						ps1.setString(1, "api_key");
						ps1.setString(2, key.toString());
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2015;
					updateDBBuild(dbBuild);
				}
				
				//This block adds friend_status column in friends table
				if(dbBuild < 2016){
					
					// Adding friends signup status column
					String sqlAddFriendStatus = "ALTER TABLE `friends` ADD `friend_status` ENUM('pending','signedup') NOT NULL DEFAULT 'pending' AFTER `friend_user_id`";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddFriendStatus);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2016;
					updateDBBuild(dbBuild);
				}
				
				if(dbBuild < 2017){
					
					String sqlNotificationTable = "CREATE TABLE `fls`.`events` ( `event_id` INT(32) NOT NULL AUTO_INCREMENT , `datetime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , `from_user_id` VARCHAR(255) NULL , `to_user_id` VARCHAR(255) NULL , `event_type` ENUM('FLS_EVENT_NOT_NOTIFICATION','FLS_EVENT_NOTIFICATION','FLS_EVENT_CHAT') NOT NULL , `read_status` ENUM('FLS_READ','FLS_UNREAD') NOT NULL DEFAULT 'FLS_UNREAD' , `delivery_status` ENUM('FLS_DELIVERED','FLS_UNDELIVERED') NOT NULL DEFAULT 'FLS_UNDELIVERED' , `notification_type` ENUM('FLS_MAIL_FORGOT_PASSWORD','FLS_MAIL_SIGNUP_VALIDATION','FLS_MAIL_REGISTER','FLS_MAIL_DELETE_ITEM','FLS_MAIL_POST_ITEM','FLS_MAIL_MATCH_WISHLIST_ITEM','FLS_MAIL_MATCH_POST_ITEM','FLS_MAIL_ADD_FRIEND_FROM','FLS_MAIL_ADD_FRIEND_TO','FLS_MAIL_DELETE_FRIEND_FROM','FLS_MAIL_DELETE_FRIEND_TO','FLS_MAIL_REJECT_REQUEST_FROM','FLS_MAIL_REJECT_REQUEST_TO','FLS_MAIL_DELETE_REQUEST_FROM','FLS_MAIL_DELETE_REQUEST_TO','FLS_MAIL_GRANT_LEASE_FROM','FLS_MAIL_GRANT_LEASE_TO','FLS_MAIL_REJECT_LEASE_FROM','FLS_MAIL_REJECT_LEASE_TO','FLS_MAIL_GRACE_PERIOD_OWNER','FLS_MAIL_GRACE_PERIOD_REQUESTOR','FLS_MAIL_RENEW_LEASE_OWNER','FLS_MAIL_RENEW_LEASE_REQUESTOR','FLS_MAIL_MAKE_REQUEST_FROM','FLS_MAIL_MAKE_REQUEST_TO') NOT NULL , `item_id` INT(32) NULL , `message` TEXT NULL , `archived` ENUM('FLS_ACTIVE','FLS_ARCHIVED') NOT NULL DEFAULT 'FLS_ACTIVE' , PRIMARY KEY (`event_id`))";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlNotificationTable);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2017;
					updateDBBuild(dbBuild);
					
				}
				
				// This block adds user_notification column in users table
				if(dbBuild < 2018){
					
					String sqlUserNotification = "ALTER TABLE `users` ADD `user_notification` ENUM('EMAIL','SMS','BOTH','NONE') NOT NULL DEFAULT 'EMAIL' AFTER `user_live_status`";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlUserNotification);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2018;
					updateDBBuild(dbBuild);
					
				}
				
				// This block adds notification_type enum in events table
				if(dbBuild < 2019){
					
					String sqlAddNotificationEnum = "ALTER TABLE `events` CHANGE `notification_type` `notification_type` ENUM('FLS_MAIL_FORGOT_PASSWORD','FLS_MAIL_SIGNUP_VALIDATION','FLS_MAIL_REGISTER','FLS_MAIL_DELETE_ITEM','FLS_MAIL_POST_ITEM','FLS_MAIL_MATCH_WISHLIST_ITEM','FLS_MAIL_MATCH_POST_ITEM','FLS_MAIL_ADD_FRIEND_FROM','FLS_MAIL_ADD_FRIEND_TO','FLS_MAIL_DELETE_FRIEND_FROM','FLS_MAIL_DELETE_FRIEND_TO','FLS_MAIL_REJECT_REQUEST_FROM','FLS_MAIL_REJECT_REQUEST_TO','FLS_MAIL_DELETE_REQUEST_FROM','FLS_MAIL_DELETE_REQUEST_TO','FLS_MAIL_GRANT_LEASE_FROM','FLS_MAIL_GRANT_LEASE_TO','FLS_MAIL_REJECT_LEASE_FROM','FLS_MAIL_REJECT_LEASE_TO','FLS_MAIL_GRACE_PERIOD_OWNER','FLS_MAIL_GRACE_PERIOD_REQUESTOR','FLS_MAIL_RENEW_LEASE_OWNER','FLS_MAIL_RENEW_LEASE_REQUESTOR','FLS_MAIL_MAKE_REQUEST_FROM','FLS_MAIL_MAKE_REQUEST_TO','FLS_NOMAIL_ADD_WISH_ITEM','FLS_SMS_FORGOT_PASSWORD','FLS_SMS_SIGNUP_VALIDATION','FLS_SMS_REGISTER')";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddNotificationEnum);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2019;
					updateDBBuild(dbBuild);
					
				}
				
				// This block creates an email column in users table
				if(dbBuild < 2020){
					
					String sqlCreateUserEmailColumn = "ALTER TABLE `users` ADD `user_email` VARCHAR(255) NULL DEFAULT NULL AFTER `user_mobile`, ADD `user_sec_status` ENUM('1','0') NOT NULL DEFAULT '0' AFTER `user_email`";
					String sqlCopyEmailsFromUserId = "UPDATE `users` SET `user_email` = `user_id`";
					String sqlChangeUserStatusEnum = "ALTER TABLE `users` CHANGE `user_status` `user_status` ENUM('google','facebook','email_pending','email_activated','mobile_pending','mobile_activated')";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlCreateUserEmailColumn);
						ps1.executeUpdate();
						ps1.close();
						PreparedStatement ps2 = connection.prepareStatement(sqlCopyEmailsFromUserId);
						ps2.executeUpdate();
						ps2.close();
						PreparedStatement ps3 = connection.prepareStatement(sqlChangeUserStatusEnum);
						ps3.executeUpdate();
						ps3.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2020;
					updateDBBuild(dbBuild);
				}
				
				// This block adds notification_type enum in events table
				if(dbBuild < 2021){
					
					String sqlAddNotificationEnum = "ALTER TABLE `events` CHANGE `notification_type` `notification_type` ENUM('FLS_MAIL_FORGOT_PASSWORD','FLS_MAIL_SIGNUP_VALIDATION','FLS_MAIL_REGISTER','FLS_MAIL_DELETE_ITEM','FLS_MAIL_POST_ITEM','FLS_MAIL_MATCH_WISHLIST_ITEM','FLS_MAIL_MATCH_POST_ITEM','FLS_MAIL_ADD_FRIEND_FROM','FLS_MAIL_ADD_FRIEND_TO','FLS_MAIL_DELETE_FRIEND_FROM','FLS_MAIL_DELETE_FRIEND_TO','FLS_MAIL_REJECT_REQUEST_FROM','FLS_MAIL_REJECT_REQUEST_TO','FLS_MAIL_DELETE_REQUEST_FROM','FLS_MAIL_DELETE_REQUEST_TO','FLS_MAIL_GRANT_LEASE_FROM','FLS_MAIL_GRANT_LEASE_TO','FLS_MAIL_REJECT_LEASE_FROM','FLS_MAIL_REJECT_LEASE_TO','FLS_MAIL_GRACE_PERIOD_OWNER','FLS_MAIL_GRACE_PERIOD_REQUESTOR','FLS_MAIL_RENEW_LEASE_OWNER','FLS_MAIL_RENEW_LEASE_REQUESTOR','FLS_MAIL_MAKE_REQUEST_FROM','FLS_MAIL_MAKE_REQUEST_TO','FLS_NOMAIL_ADD_WISH_ITEM','FLS_SMS_FORGOT_PASSWORD','FLS_SMS_SIGNUP_VALIDATION','FLS_SMS_REGISTER','FLS_EMAIL_VERIFICATION','FLS_MOBILE_VERIFICATION')";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddNotificationEnum);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2021;
					updateDBBuild(dbBuild);
					
				}
				
				// This block adds promo codes for sharing and invitation
				if(dbBuild < 2022){
					
					String sqlAddPromoCode = "INSERT INTO `promo_credits` (`code`, `credit`, `expiry`) VALUES ('shared@10', '10', '3000-01-01') , ('invited@10', '10', '3000-01-01')";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddPromoCode);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2022;
					updateDBBuild(dbBuild);
					
				}
				
				// This block adds promo codes for sharing and invitation
				if(dbBuild < 2023){
					
					String sqlAddFbIdUsers = "ALTER TABLE `users` ADD `user_fb_id` VARCHAR(255) NULL DEFAULT NULL AFTER `user_notification`";
					String sqlAddFbIdFriends = "ALTER TABLE `friends` ADD `friend_fb_id` VARCHAR(255) NULL DEFAULT NULL AFTER `friend_status`";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddFbIdUsers);
						ps1.executeUpdate();
						ps1.close();
						PreparedStatement ps2 = connection.prepareStatement(sqlAddFbIdFriends);
						ps2.executeUpdate();
						ps2.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2023;
					updateDBBuild(dbBuild);
					
				}
				
				// This block adds a column item_image_links
				if(dbBuild < 2024){
					
					String sqlCreateLinksColumn = "ALTER TABLE `items` ADD `item_image_links` VARCHAR(1024) NULL DEFAULT NULL AFTER `item_image`";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlCreateLinksColumn);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2024;
					updateDBBuild(dbBuild);
					
				}
				
				// This block adds a uploads all images to s3 and saves their link in items table
				if(dbBuild < 2025){
					
					String sqlSelectAllItems = "SELECT item_uid, item_image FROM items";
					
					PreparedStatement ps1 = null, ps2 = null;
					ResultSet rs1 = null;
					
					try{
						getConnection();
						ps1 = connection.prepareStatement(sqlSelectAllItems);
						rs1 = ps1.executeQuery();
						
						while(rs1.next()){
							String image = rs1.getString("item_image");
							if(image != null && !image.isEmpty() && !image.equals("null")){
								FlsS3Bucket s3Bucket = new FlsS3Bucket(rs1.getString("item_uid"));
								String link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_POST, File_Name.ITEM_NORMAL, rs1.getString("item_image"), null);
								if(link != null){
									String sqlSaveImageLink = "UPDATE items SET item_image_links=? WHERE item_uid=?";
									ps2 = connection.prepareStatement(sqlSaveImageLink);
									ps2.setString(1, link);
									ps2.setString(2, rs1.getString("item_uid"));
									
									System.out.println(ps2.executeUpdate());
								}
							}
						}
						
					}catch(Exception e){
						e.printStackTrace();
						System.exit(1);
					}finally {
						try {
							if(ps2 != null) ps2.close();
							if(rs1 != null) rs1.close();
							if(ps1 != null) ps1.close();
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2025;
					updateDBBuild(dbBuild);
				}
				
				if(dbBuild < 2026){
					
					String sqlAddNotificationEnum = "ALTER TABLE `events` CHANGE `notification_type` `notification_type` ENUM('FLS_MAIL_FORGOT_PASSWORD','FLS_MAIL_SIGNUP_VALIDATION','FLS_MAIL_REGISTER','FLS_MAIL_DELETE_ITEM','FLS_MAIL_POST_ITEM','FLS_MAIL_MATCH_WISHLIST_ITEM','FLS_MAIL_MATCH_POST_ITEM','FLS_MAIL_ADD_FRIEND_FROM','FLS_MAIL_ADD_FRIEND_TO','FLS_MAIL_DELETE_FRIEND_FROM','FLS_MAIL_DELETE_FRIEND_TO','FLS_MAIL_REJECT_REQUEST_FROM','FLS_MAIL_REJECT_REQUEST_TO','FLS_MAIL_DELETE_REQUEST_FROM','FLS_MAIL_DELETE_REQUEST_TO','FLS_MAIL_GRANT_LEASE_FROM','FLS_MAIL_GRANT_LEASE_TO','FLS_MAIL_REJECT_LEASE_FROM','FLS_MAIL_REJECT_LEASE_TO','FLS_MAIL_GRACE_PERIOD_OWNER','FLS_MAIL_GRACE_PERIOD_REQUESTOR','FLS_MAIL_RENEW_LEASE_OWNER','FLS_MAIL_RENEW_LEASE_REQUESTOR','FLS_MAIL_MAKE_REQUEST_FROM','FLS_MAIL_MAKE_REQUEST_TO','FLS_NOMAIL_ADD_WISH_ITEM','FLS_SMS_FORGOT_PASSWORD','FLS_SMS_SIGNUP_VALIDATION','FLS_SMS_REGISTER','FLS_EMAIL_VERIFICATION','FLS_MOBILE_VERIFICATION','FLS_MAIL_MESSAGE_FRIEND_FROM','FLS_MAIL_MESSAGE_FRIEND_TO','FLS_MAIL_MESSAGE_ITEM_FROM','FLS_MAIL_MESSAGE_ITEM_TO')";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddNotificationEnum);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2026;
					updateDBBuild(dbBuild);
				}
				
				// This block adds full name to friends table where name is empty
				if(dbBuild < 2027){
					
					PreparedStatement afps1 = null, afps2 = null;
					ResultSet afrs1 = null;
					
					String sqlAddFriendsName = "SELECT * FROM `friends` WHERE friend_full_name ='-' AND friend_id LIKE '%@%'";
					try{
						getConnection();
						afps1 = connection.prepareStatement(sqlAddFriendsName);
						afrs1 = afps1.executeQuery();
						
						while(afrs1.next()){
							String friendId = afrs1.getString("friend_id");
							String[] parts = friendId.split("@");
							String friendName= parts[0];
								if(friendName != null){
									String sqlSaveFriendName = "UPDATE friends SET friend_full_name=? WHERE friend_id=?";
									afps2 = connection.prepareStatement(sqlSaveFriendName);
									afps2.setString(1, friendName);
									afps2.setString(2, friendId);
									afps2.executeUpdate();
								}
							
						}
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							if(afps2 != null) afps2.close();
							if(afrs1 != null) afrs1.close();
							if(afps1 != null) afps1.close();
							// close and reset connection to null
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2027;
					updateDBBuild(dbBuild);
				}

				//This block creates column for primary images link and renames the existing images in s3
				if(dbBuild < 2028){
					
					String sqlCreatePrimaryImageColumn = "ALTER TABLE `items` ADD `item_primary_image_link` VARCHAR(255) NULL DEFAULT NULL AFTER `item_image`";
					String sqlSelectAllItems = "SELECT item_uid, item_image_links FROM items";
					
					PreparedStatement ps1 = null, ps2 = null, ps3 = null;
					ResultSet rs2 = null;
					
					try{
						getConnection();
						ps1 = connection.prepareStatement(sqlCreatePrimaryImageColumn);
						ps1.executeUpdate();
						
						ps2 = connection.prepareStatement(sqlSelectAllItems);
						rs2 = ps2.executeQuery();
						
						while(rs2.next()){
							String imageLink = rs2.getString("item_image_links");
							if(imageLink != null && !imageLink.isEmpty() && !imageLink.equals("null")){
								FlsS3Bucket s3Bucket = new FlsS3Bucket(rs2.getString("item_uid"));
								String link = s3Bucket.copyImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_POST, File_Name.ITEM_PRIMARY, imageLink);
								if(link != null){
									s3Bucket.deleteImage(Bucket_Name.ITEMS_BUCKET, imageLink);
									String sqlSaveImageLink = "UPDATE items SET item_primary_image_link=?, item_image_links=? WHERE item_uid=?";
									ps3 = connection.prepareStatement(sqlSaveImageLink);
									ps3.setString(1, link);
									ps3.setString(2, null);
									ps3.setString(3, rs2.getString("item_uid"));
									
									System.out.println(ps3.executeUpdate());
								}
							}
						}
						
					}catch(Exception e){
						e.printStackTrace();
						System.exit(1);
					}finally {
						try {
							if(ps3 != null) ps3.close();
							if(rs2 != null) rs2.close();
							if(ps2 != null) ps2.close();
							if(ps1 != null) ps1.close();
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2028;
					updateDBBuild(dbBuild);
				}
				
				// This block creates a table for image links
				if(dbBuild < 2029){
					
					String sqlImageLinksTable = "CREATE TABLE `fls`.`images` (`item_id` INT NOT NULL AUTO_INCREMENT , `item_uid` VARCHAR(255) NULL DEFAULT NULL , `item_image_link` VARCHAR(255) NULL DEFAULT NULL , PRIMARY KEY (`item_id`))";
					String sqlDeleteImageLinksColumn = "ALTER TABLE `items` DROP COLUMN `item_image_links`";
					
					PreparedStatement ps1 = null, ps2 = null;
					
					try{
						getConnection();
						ps1 = connection.prepareStatement(sqlImageLinksTable);
						ps1.executeUpdate();
						
						ps2 = connection.prepareStatement(sqlDeleteImageLinksColumn);
						ps2.executeUpdate();
						
					}catch(Exception e){
						e.printStackTrace();
						System.exit(1);
					}finally {
						try {
							if(ps2 != null) ps2.close();
							if(ps1 != null) ps1.close();
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2029;
					updateDBBuild(dbBuild);
					
				}
				
				// This block creates a table for items rating
				if(dbBuild < 2030){
					
					String sqlItemsRatingTable = "CREATE TABLE `fls`.`items_rating` ( `rating_id` INT NOT NULL AUTO_INCREMENT , `item_id` INT(32) NULL , `leasee_id` VARCHAR(255) NULL DEFAULT NULL , `item_rating` ENUM('1','2','3','4') NOT NULL , `feedback` VARCHAR(255) NULL DEFAULT NULL , `datetime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY (`rating_id`)) ";
					
					PreparedStatement ps1 = null;
					
					try{
						getConnection();
						ps1 = connection.prepareStatement(sqlItemsRatingTable);
						ps1.executeUpdate();
						
					}catch(Exception e){
						e.printStackTrace();
						System.exit(1);
					}finally {
						try {
							if(ps1 != null) ps1.close();
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2030;
					updateDBBuild(dbBuild);
					
				}

				// This block creates a table for email newsletter
				if(dbBuild < 2031){
					
					String sqlLeadTable = "CREATE TABLE `fls`.`leads` ( `lead_id` INT NOT NULL AUTO_INCREMENT , `lead_email` VARCHAR(255), `lead_type` VARCHAR(255) NULL DEFAULT NULL , `lead_datetime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY (`lead_id`)) ";
					
					PreparedStatement ps1 = null;
					
					try{
						getConnection();
						ps1 = connection.prepareStatement(sqlLeadTable);
						ps1.executeUpdate();
						
					}catch(Exception e){
						e.printStackTrace();
						System.exit(1);
					}finally {
						try {
							if(ps1 != null) ps1.close();
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2031;
					updateDBBuild(dbBuild);
				}
				
				// This block creates last modified columns
				if(dbBuild < 2032){
					
					String sqlItemsLastmodified = "ALTER TABLE `items` ADD `item_lastmodified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `item_lng`";
					String sqlRequestsLastmodified = "ALTER TABLE `requests` ADD `request_lastmodified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `request_status`";
					
					PreparedStatement ps1 = null, ps2 = null;
					
					try{
						getConnection();
						ps1 = connection.prepareStatement(sqlItemsLastmodified);
						ps1.executeUpdate();
						
						ps2 = connection.prepareStatement(sqlRequestsLastmodified);
						ps2.executeUpdate();
					}catch(Exception e){
						e.printStackTrace();
						System.exit(1);
					}finally {
						try {
							if(ps2 != null) ps2.close();
							if(ps1 != null) ps1.close();
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					
					// The dbBuild version value is changed in the database
					dbBuild = 2032;
					updateDBBuild(dbBuild);
					
				}
				
				// This block creates notifications types for delete job in events table
				if(dbBuild < 2033){
					
					String sqlAddNotificationEnum = "ALTER TABLE `events` CHANGE `notification_type` `notification_type` ENUM('FLS_MAIL_FORGOT_PASSWORD','FLS_MAIL_SIGNUP_VALIDATION','FLS_MAIL_REGISTER','FLS_MAIL_DELETE_ITEM','FLS_MAIL_POST_ITEM','FLS_MAIL_MATCH_WISHLIST_ITEM','FLS_MAIL_MATCH_POST_ITEM','FLS_MAIL_ADD_FRIEND_FROM','FLS_MAIL_ADD_FRIEND_TO','FLS_MAIL_DELETE_FRIEND_FROM','FLS_MAIL_DELETE_FRIEND_TO','FLS_MAIL_REJECT_REQUEST_FROM','FLS_MAIL_REJECT_REQUEST_TO','FLS_MAIL_DELETE_REQUEST_FROM','FLS_MAIL_DELETE_REQUEST_TO','FLS_MAIL_GRANT_LEASE_FROM','FLS_MAIL_GRANT_LEASE_TO','FLS_MAIL_REJECT_LEASE_FROM','FLS_MAIL_REJECT_LEASE_TO','FLS_MAIL_GRACE_PERIOD_OWNER','FLS_MAIL_GRACE_PERIOD_REQUESTOR','FLS_MAIL_RENEW_LEASE_OWNER','FLS_MAIL_RENEW_LEASE_REQUESTOR','FLS_MAIL_MAKE_REQUEST_FROM','FLS_MAIL_MAKE_REQUEST_TO','FLS_NOMAIL_ADD_WISH_ITEM','FLS_SMS_FORGOT_PASSWORD','FLS_SMS_SIGNUP_VALIDATION','FLS_SMS_REGISTER','FLS_EMAIL_VERIFICATION','FLS_MOBILE_VERIFICATION','FLS_MAIL_MESSAGE_FRIEND_FROM','FLS_MAIL_MESSAGE_FRIEND_TO','FLS_MAIL_MESSAGE_ITEM_FROM','FLS_MAIL_MESSAGE_ITEM_TO','FLS_MAIL_OLD_ITEM_WARN','FLS_MAIL_OLD_REQUEST_WARN','FLS_MAIL_OLD_LEASE_WARN')";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlAddNotificationEnum);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2033;
					updateDBBuild(dbBuild);
					
				}
				
				// This block creates column for user plan
				if(dbBuild < 2034){
					
					String sqlPlacesTable = "CREATE TABLE `fls`.`places` ( `place_id` INT NOT NULL AUTO_INCREMENT , `locality` VARCHAR(255) NULL , PRIMARY KEY (`place_id`))";
					String sqlUsersPlanColumn = "ALTER TABLE `users` ADD `user_plan` ENUM('FLS_SELFIE','FLS_PRIME','FLS_UBER') NOT NULL DEFAULT 'FLS_SELFIE' AFTER `user_id`";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlPlacesTable);
						ps1.executeUpdate();
						ps1.close();
						
						PreparedStatement ps2 = connection.prepareStatement(sqlUsersPlanColumn);
						ps2.executeUpdate();
						ps2.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2034;
					updateDBBuild(dbBuild);
					
				}
				
				// This block creates column for image link in Item log
				if(dbBuild < 2035){
					
					String sqlImageLink = "ALTER TABLE item_log ADD item_log_image_link VARCHAR(255) after item_log_image";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlImageLink);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2035;
					updateDBBuild(dbBuild);
					
				}
				
				// This block deletes request_date column from requests table
				if(dbBuild < 2036){
					
					String sqlDeleteRequestDate = "ALTER TABLE `requests` DROP `request_date`";
					String sqlAddPlace = "INSERT INTO `places` (`locality`) VALUES ('Pune')";
					
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(sqlDeleteRequestDate);
						ps1.executeUpdate();
						ps1.close();
						
						PreparedStatement ps2 = connection.prepareStatement(sqlAddPlace);
						ps2.executeUpdate();
						ps2.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2036;
					updateDBBuild(dbBuild);
					
				}
				
				// This block alters column image link in Item log
				if(dbBuild < 2037){
					
					String updateDatetimeDefault = "ALTER TABLE item_log MODIFY COLUMN item_log_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
					try{
						getConnection();
						PreparedStatement ps1 = connection.prepareStatement(updateDatetimeDefault);
						ps1.executeUpdate();
						ps1.close();
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getStackTrace());
						System.exit(1);
					}finally {
						try {
							connection.close();
							connection = null;
							} catch (Exception e){
								e.printStackTrace();
								System.out.println(e.getStackTrace());
							}
					}
					// The dbBuild version value is changed in the database
					dbBuild = 2037;
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
		} catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getStackTrace());
		} finally {
			try {
				// close and reset connection to null
				connection.close();
				connection = null;
			} catch (Exception e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}
		}
	}
	
}
