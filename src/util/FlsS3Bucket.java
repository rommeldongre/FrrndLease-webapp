package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;

import connect.Connect;

public class FlsS3Bucket extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsS3Bucket.class.getName());

	private String ENV_CONFIG = FlsConfig.env;
	
	private String uid = null;
	private int leaseId = -1;
	
	private String BASE_URL = "http://s3-ap-southeast-1.amazonaws.com/";
	
	private BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJMC2WRKPB6UEF5DA","obuoy6YGu6YUSLRrIHoe7E5M9QdzyKjZ1u0mc6m2");
	private AmazonS3 s3Client = new AmazonS3Client(credentials);
	
	public enum Bucket_Name{
		ITEMS_BUCKET,
		USERS_BUCKET
	}
	
	public enum Path_Name{
		ITEM_POST,
		ITEM_LEASE
	}
	
	public enum File_Name{
		ITEM_PRIMARY,
		ITEM_NORMAL,
		LEASE_READY,
		PICKED_UP_OUT,
		LEASE_STARTED,
		LEASE_ENDED,
		PICKED_UP_IN
	}
	
	public FlsS3Bucket(String Uid){
		this.uid = Uid;
	}
	
	public FlsS3Bucket(String Uid, int LeaseId){
		this.uid = Uid;
		this.leaseId = LeaseId;
	}
	
	private String getBucketName(Bucket_Name bucketName){
		
		LOGGER.info("Inside getBucketName method");
		
		if(bucketName == Bucket_Name.ITEMS_BUCKET){
			if(ENV_CONFIG.equals("dev")){
				return "fls-items-dev";
			}else{
				return "fls-items-live";
			}
		}else if(bucketName == Bucket_Name.USERS_BUCKET){
			if(ENV_CONFIG.equals("dev")){
				return "fls-users-dev";
			}else{
				return "fls-users-live";
			}
		}else{
			return null;
		}
		
	}
	
	private String getPathName(Path_Name pathName){
		
		LOGGER.info("Inside getPathName method");
		
		String path = uid + "/";
		
		if(pathName == Path_Name.ITEM_POST)
			path = path + "post/";
		else if(pathName == Path_Name.ITEM_LEASE)
			path = path + "lease-" + leaseId + "/";
		else path = null;
		
		return path;
	}
	
	private String getFileName(File_Name fileName){
		
		LOGGER.info("Inside getFileName method");
		
		String name = uid + "-";
		
		Random rnd = new Random();
		int r = 1000 + rnd.nextInt(9000);
		
		if(fileName == File_Name.ITEM_PRIMARY){
			name = name + "primary-" + r + ".png";
		}else if(fileName == File_Name.ITEM_NORMAL){
			name = name + r + ".png";
		}else if(fileName == File_Name.LEASE_READY)
			name = name + "LeaseReady.png";
		else if(fileName == File_Name.PICKED_UP_OUT)
			name = name + "PickedUpOut.png";
		else if(fileName == File_Name.LEASE_STARTED)
			name = name + "LeaseStarted.png";
		else if(fileName == File_Name.LEASE_ENDED)
			name = name + "LeaseEnded.png";
		else if(fileName == File_Name.PICKED_UP_IN)
			name = name + "PickedUpIn.png";
		else
			name = null;
			
		return name;
		
	}
	
	public String uploadImage(Bucket_Name bucketName, Path_Name pathName, File_Name fileName, String Image, String existingName) {

		LOGGER.info("Inside uploadImage method");

		String BUCKET_NAME = getBucketName(bucketName);
		String PATH_NAME = getPathName(pathName);
		String FILE_NAME;

		if(existingName == null)
			FILE_NAME = getFileName(fileName);
		else
			FILE_NAME = existingName.substring(existingName.lastIndexOf("/")+1);
		
		try {
			
			LOGGER.warning("Bucket Name : " + BUCKET_NAME + ", Path Name : " + PATH_NAME + ", File Name : " + FILE_NAME);
			
			if(BUCKET_NAME != null && PATH_NAME != null && FILE_NAME != null && Image != null){
				
				File imageFile = convertBinaryToImage(Image, FILE_NAME);
	
				s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
	
				if (s3Client.doesBucketExist(BUCKET_NAME)) {
					LOGGER.info("bucket exists: " + BUCKET_NAME);
				} else {
					LOGGER.warning("bucket does not exist: " + BUCKET_NAME);
					return null;
				}
				
				if (imageFile != null) {
					s3Client.putObject(new PutObjectRequest(BUCKET_NAME, PATH_NAME + FILE_NAME, imageFile).withCannedAcl(CannedAccessControlList.PublicRead));
					return BASE_URL + BUCKET_NAME + "/" + PATH_NAME + FILE_NAME;
				}
				
			}else{
				if(Image == null)
					LOGGER.info("Image Link is null");
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which" + " means your request made it " + "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means" + " the client encountered " + "an internal error while trying to " + "communicate with S3, " + "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String copyImage(Bucket_Name bucketName, Path_Name pathName, File_Name fileName, String ImageLink) {

		LOGGER.info("Inside copyImage method");

		String BUCKET_NAME = getBucketName(bucketName);
		String PATH_NAME = getPathName(pathName);
		String FILE_NAME = getFileName(fileName);
		
		try {
			
			LOGGER.warning("Bucket Name : " + BUCKET_NAME + ", Path Name : " + PATH_NAME + ", File Name : " + FILE_NAME);
			
			if(BUCKET_NAME != null && PATH_NAME != null && FILE_NAME != null && ImageLink != null){
				
				String existingKey = ImageLink.substring(ordinalIndexOf(ImageLink, "/", 3)+1);
	
				s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
	
				if (s3Client.doesBucketExist(BUCKET_NAME)) {
					LOGGER.info("bucket exists: " + BUCKET_NAME);
				} else {
					LOGGER.warning("bucket does not exist: " + BUCKET_NAME);
					return null;
				}
				
				AccessControlList acl = new AccessControlList();
				acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
				
				// Copying object
	            CopyObjectRequest copyObjRequest = new CopyObjectRequest(BUCKET_NAME, existingKey, BUCKET_NAME, PATH_NAME+FILE_NAME);
	            LOGGER.info("Copying Image.");
	            s3Client.copyObject(copyObjRequest.withAccessControlList(acl));
	            return BASE_URL + BUCKET_NAME + "/" + PATH_NAME + FILE_NAME;
				
			}else{
				if(ImageLink == null)
					LOGGER.info("Image Link is null");
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which" + " means your request made it " + "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means" + " the client encountered " + "an internal error while trying to " + "communicate with S3, " + "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public int deleteImage(Bucket_Name bucketName, String ImageLink){
		
		LOGGER.info("Inside deleteImage method");

		String BUCKET_NAME = getBucketName(bucketName);
		
		try {
			
			LOGGER.warning("Bucket Name : " + BUCKET_NAME);
			
			if(BUCKET_NAME != null || ImageLink != null){
	
				String existingKey = ImageLink.substring(ordinalIndexOf(ImageLink, "/", 3)+1);
				
				s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
	
				if (s3Client.doesBucketExist(BUCKET_NAME)) {
					LOGGER.info("bucket exists: " + BUCKET_NAME);
				} else {
					LOGGER.warning("bucket does not exist: " + BUCKET_NAME);
					return 0;
				}
				
				// Deleting object
	            DeleteObjectRequest deleteObjRequest = new DeleteObjectRequest(BUCKET_NAME, existingKey);
	            LOGGER.info("Deleting Image.");
	            s3Client.deleteObject(deleteObjRequest);
	            return 1;
				
			}

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which" + " means your request made it " + "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means" + " the client encountered " + "an internal error while trying to " + "communicate with S3, " + "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e){
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private int ordinalIndexOf(String str, String s, int n) {
	    int pos = str.indexOf(s, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(s, pos+1);
	    return pos;
	}
	
	public void savePrimaryImageLink(String link){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1 = 0;
		
		if(link.isEmpty() || link.equals("null"))
			return;
		
		try{
			
			String sqlSaveImageLinks = "UPDATE items SET item_primary_image_link=? WHERE item_uid=?";
			ps1 = hcp.prepareStatement(sqlSaveImageLinks);
			ps1.setString(1, link);
			ps1.setString(2, uid);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1){
				LOGGER.info("Item uid : " + uid + " primary image link updated to : " + link);
			}else{
				LOGGER.info("Item uid : " + uid + " primary image link not updated");
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void saveNormalImageLink(String links){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2 = 0;
		
		if(links.isEmpty() || links.equals("null"))
			return;
		
		try{
			
			String sqlSelectImageLinks = "SELECT item_image_Links FROM items WHERE item_uid=?";
			ps1 = hcp.prepareStatement(sqlSelectImageLinks);
			ps1.setString(1, uid);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				String existingLinks = rs1.getString("item_image_links");
				if(existingLinks != null)
					existingLinks = existingLinks + "," + links;
				else
					existingLinks = links;
				
				String sqlSaveImageLinks = "UPDATE items SET item_image_links=? WHERE item_uid=?";
				ps2 = hcp.prepareStatement(sqlSaveImageLinks);
				ps2.setString(1, existingLinks);
				ps2.setString(2, uid);
				
				rs2 = ps2.executeUpdate();
			}
			
			if(rs2 == 1){
				LOGGER.info("Item uid : " + uid + " links updated to : " + links);
			}else{
				LOGGER.info("Item uid : " + uid + " links not updated");
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
		}finally{
			try{
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void deletePrimaryImageLink(){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		
		try{
			
			String sqlDeletePrimaryImage = "UPDATE items SET item_primary_image_link=? WHERE item_uid=?";
			ps1 = hcp.prepareStatement(sqlDeletePrimaryImage);
			ps1.setString(1, uid);
			
			int result = ps1.executeUpdate();
			
			if(result == 1){
				LOGGER.info("Item's primary image link deleted");
			}else{
				LOGGER.info("Item's primary image link not deleted");
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void deleteNormalImageLink(String link){

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		
		if(link.isEmpty() || link.equals("null"))
			return;
		
		try{
			
			String sqlSelectImageLinks = "SELECT item_image_links FROM items WHERE item_uid=?";
			ps1 = hcp.prepareStatement(sqlSelectImageLinks);
			ps1.setString(1, uid);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				
				String existingLinks = rs1.getString("item_image_links");
				
				String newLinks = "";
				
				String[] arr;
				
				if(existingLinks != null){
					arr = existingLinks.split(",");
					int i = 0;
					for(String e : arr){
						if(e.equals(link)){
							arr[i] = null;
							break;
						}
						i++;
					}
					if(arr.length == 0){
						newLinks = null;
					}else{
						for(int j = 0; j < arr.length; j++){
							if(j == 0)
								newLinks = arr[j];
							else
								newLinks = newLinks + "," + arr[j];
						}
					}
				}
				
				if(!newLinks.isEmpty()){
					String sqlDeletePrimaryImage = "UPDATE items SET item_image_links=? WHERE item_uid=?";
					ps2 = hcp.prepareStatement(sqlDeletePrimaryImage);
					ps2.setString(1, newLinks);
					ps2.setString(2, uid);
					
					int result = ps1.executeUpdate();
					
					if(result == 1){
						LOGGER.info("Item's image link - " + link + " deleted");
					}else{
						LOGGER.info("Item's image link - " + link + " not deleted");
					}
				}
				
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
		}finally{
			try{
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private File convertBinaryToImage(String imageString, String FileName) {

		if (imageString == null || imageString == "") {
			return null;
		}

		try {
			LOGGER.warning("decoding the image");
			
			String[] i = imageString.split(",");
			String binary = i[1];

			BufferedImage image = null;
			byte[] imageByte;

			imageByte = DatatypeConverter.parseBase64Binary(binary);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			image = ImageIO.read(bis);
			bis.close();

			// write the image to a file
			File file = new File(FileName);
			ImageIO.write(image, "png", file);

			return file;
		} catch (IOException e) {
			LOGGER.warning("Not able to decode the image");
			e.printStackTrace();
		}

		return null;
	}

}
