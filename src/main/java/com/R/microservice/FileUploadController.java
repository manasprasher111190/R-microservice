package com.R.microservice;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {
	
	public static final String SERVER_DATA_DIR = "/home/manas/dataServer";

    @RequestMapping(value="/upload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public @ResponseBody String handleFileUpload(@RequestParam("name") String name,
    		@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(new File(Paths.get(
								SERVER_DATA_DIR, name).toString())));
                stream.write(bytes);
                stream.close();
                return "You successfully uploaded " + name + "!";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }
    
    @RequestMapping(value="/fileList")
    public @ResponseBody String listFileinServerDirectory(){
    	try(Stream<Path> list = Files.list(Paths.get(SERVER_DATA_DIR))){
    		Optional<Path> findFirst = list.findFirst();
			if(findFirst.isPresent()){
    			return findFirst.get().toString();
    		}else {
    			return "NONE";
    		}
    	}catch(IOException e){
    		return e.getMessage();
    	}
    }
    
    @RequestMapping(value="/delete")
    public @ResponseBody boolean deleteFile(@RequestParam(value = "path",required = true)String path) throws IOException{
    	return Files.deleteIfExists(Paths.get(URI.create(path)));
    }

}
