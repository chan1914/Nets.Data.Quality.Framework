package nets.CSV.webApplication.Controller;

import nets.CSV.webApplication.CSVDigester.CSVDigester;
import nets.CSV.webApplication.filestorage.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UploadFileController {

    @Autowired
    FileStorage fileStorage;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CSVDigester csvDigester;

    Logger logger = LoggerFactory.getLogger(UploadFileController.class);

    @GetMapping("/")
    public String index() {
        return "uploadform.html";
    }

    @PostMapping("/")
    public String upload(@RequestParam("uploadfile") MultipartFile file, Model model) {
        try {
            fileStorage.store(file);
            model.addAttribute("message", "File uploaded successfully! -> filename = " + file.getOriginalFilename());


        } catch (Exception e) {
            model.addAttribute("message", "Fail! -> uploaded filename: " + file.getOriginalFilename());
            return "uploadform";
        }

        try {
            String file1 = Files.readString(Paths.get("filestorage/" + file.getOriginalFilename()));
            List<String> rows = csvDigester.CSVToJSON(file1);
            logger.info("Sending rows\t" + rows.size());
            for(String row : rows){
                restTemplate.postForObject("http://DQF_Analysis_Core/row/", row, String.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "uploadform";
    }




    /*
    @PostMapping("/upload-csv-file")
    public String UploadCSVFile(@RequestParam("uploadfile") MultipartFile file, Model model) {
        // validate file
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            model.addAttribute("status", false);
        } else {

            // parse CSV file to create a list of `User` objects
            try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

                // create csv bean reader
                CsvToBean csvToBean = new CsvToBeanBuilder(reader)
                        .withType(User.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                // convert `CsvToBean` object to list of users
                List<User> users = csvToBean.parse();

                // TODO: save users in DB?

                // save users list on model
                model.addAttribute("users", users);
                model.addAttribute("status", true);

            } catch (Exception ex) {
                model.addAttribute("message", "An error occurred while processing the CSV file.");
                model.addAttribute("status", false);
            }
        }
        return "file-upload-status";
    }
    */

}
