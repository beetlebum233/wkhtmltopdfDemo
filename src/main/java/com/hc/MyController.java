package com.hc;

import com.openhtmltopdf.DOMBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;

@RestController
public class MyController {

    @Value("${filePath}")
    public String filePath;

    @GetMapping("/getWkPdf")
    public synchronized ResponseEntity<InputStreamResource> getWkPdf(String url) throws Exception{
        String command = "wkhtmltopdf '"+ url +"'  '" + filePath +"'";
        Runtime.getRuntime().exec(command);
        File file = new File(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(file)));

    }

    @GetMapping("/getOpenPdf")
    public synchronized ResponseEntity<InputStreamResource> getOpenPdf(String url) throws Exception{
        try (OutputStream os = new FileOutputStream(filePath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
//            builder.withW3cDocument(html5ParseDocument(url, 5000), url);
            builder.withUri(url);
            builder.toStream(os);
            builder.run();
        }
        File file = new File(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(file)));

    }

    private org.w3c.dom.Document html5ParseDocument(String urlStr, int timeoutMs) throws IOException
    {
        URL url = new URL(urlStr);
        org.jsoup.nodes.Document doc;

        if (url.getProtocol().equalsIgnoreCase("file")) {
            doc = Jsoup.parse(new File(url.getPath()), "UTF-8");
        }
        else {
            doc = Jsoup.parse(url, timeoutMs);
        }

        return DOMBuilder.jsoup2DOM(doc);
    }

}
