package com.awss3.rednodes.service;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class AmazonService {

    private AmazonS3 s3client;

    @Value("${amazon.s3.region}")
    private String region;

    @Value("${amazon.s3.bucket}")
    private String bucketName;

    @Value("${amazon.aws.access-key-id}")
    private String accessKey;

    @Value("${amazon.aws.access-key-secret}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon(){
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(this.region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public String uploadFile(MultipartFile multipartFile){
        String fileUrl = "";
        try{
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            uploadFileTos3bucket(fileName, file);
            file.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
        return fileUrl;
    }

    private File convertMultiPartToFile(MultipartFile file) throws Exception{
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "_" + multiPart.getOriginalFilename().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file){
        LocalDate currentDate = LocalDate.now();
        String _month = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
//        String _day = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        Integer _day = currentDate.getDayOfMonth();
        String keyName = _month.toLowerCase() + "/" + _day + "/" + fileName;
        this.s3client.putObject(new PutObjectRequest(bucketName, keyName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public List<String> listFiles(){

        LocalDate currentDate
                = LocalDate.now();
        String _month = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        Integer _day = currentDate.getDayOfMonth();

        System.out.println(_month.toLowerCase());
        System.out.println(_day);

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                        .withBucketName(bucketName);
//                        .withPrefix("/");
        List<String> keys = new ArrayList<>();
        ObjectListing objects = s3client.listObjects(listObjectsRequest);
        while(true){
            List<S3ObjectSummary> summaries = objects.getObjectSummaries();
            if(summaries.size() < 1){
                break;
            }
            for(S3ObjectSummary item : summaries){
                if(!item.getKey().endsWith("/"))
                    keys.add(item.getKey());
            }
            objects = s3client.listNextBatchOfObjects(objects);
        }
        return keys;
    }
}
