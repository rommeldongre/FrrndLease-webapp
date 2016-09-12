package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import connect.Connect;

public class FlsS3Bucket extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsS3Bucket.class.getName());

	private String ENV_CONFIG = FlsConfig.env;
	
	private String uid = null;
	private int leaseId = -1;
	
	private BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJDC5ZDNCUEBKRIIA","jsv482TT0LwICxlrj/5aE5LKOj9VMXg3kV4dEBCc");
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
		ITEM,
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
				return "ak-fls-images";
//				return "fls-items-dev";
			}else{
				return "fls-items-live";
			}
		}else if(bucketName == Bucket_Name.USERS_BUCKET){
			if(ENV_CONFIG.equals("dev")){
				return "ak-fls-users";
//				return "fls-users-dev";
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
		
		if(fileName == File_Name.ITEM){
			Random rnd = new Random();
			int r = 1000 + rnd.nextInt(9000);
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
	
	public String uploadImage(Bucket_Name bucketName, Path_Name pathName, File_Name fileName, String Image) {

		LOGGER.info("Inside uploadImage method");

		String BUCKET_NAME = getBucketName(bucketName);
		String PATH_NAME = getPathName(pathName);
		String FILE_NAME = getFileName(fileName);
		
		try {
			
			LOGGER.warning("Bucket Name : " + BUCKET_NAME + ", Path Name : " + PATH_NAME + ", File Name : " + FILE_NAME);
			
			if(BUCKET_NAME != null && PATH_NAME != null && FILE_NAME != null){
				
				File imageFile = convertBinaryToImage(Image, FILE_NAME);
	
				s3Client.setRegion(Region.getRegion(Regions.US_WEST_2));
	
				if (s3Client.doesBucketExist(BUCKET_NAME)) {
					LOGGER.info("bucket exists: " + BUCKET_NAME);
				} else {
					LOGGER.warning("bucket does not exist: " + BUCKET_NAME);
				}
				
				if (imageFile != null) {
					s3Client.putObject(new PutObjectRequest(BUCKET_NAME, PATH_NAME + FILE_NAME, imageFile).withCannedAcl(CannedAccessControlList.PublicRead));
					return "https://s3-us-west-2.amazonaws.com/" + BUCKET_NAME + "/" + PATH_NAME + FILE_NAME;
				}
				
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
		}

		return null;
	}
	
	public String getImagesFromS3Bucket(Bucket_Name bucketName, Path_Name pathName){

		LOGGER.info("Inside getImagesFromS3Bucket method");
		
		String BUCKET_NAME = getBucketName(bucketName);
		String PATH_NAME = getPathName(pathName);
		
		String imageLinks = null;
		
		String imagelink = "https://s3-us-west-2.amazonaws.com/" + BUCKET_NAME + "/" + PATH_NAME;
		
		int i = 0;
		
		for (S3ObjectSummary summary: S3Objects.withPrefix(s3Client, BUCKET_NAME, PATH_NAME)) {
	        String s = summary.getKey();
	        s = s.substring(s.lastIndexOf("/")+1);
	        
	        if(i == 0)
	        	imageLinks = imagelink + s;
	        else
	        	imageLinks = imageLinks + "," + imagelink + s;
	        
	        i++;
		}
		
		LOGGER.info(imageLinks);
		
		return imageLinks;
		
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
