package me.j360.trace.example.springmvc;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.concurrent.Callable;

@Controller
@RequestMapping(value = "/ping", method = RequestMethod.GET)
public class PingController {


    @Autowired
    private OkHttpClient client;

    @RequestMapping(value = "/sync")
    public ResponseEntity<Void> sync() throws IOException {

        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        client.newCall(request).execute();

        Request request2 = new Request.Builder()
                .url("http://www.qq.com")
                .build();
        client.newCall(request2).execute();

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/async")
    public Callable<ResponseEntity<Void>> async() throws IOException {
        return new Callable<ResponseEntity<Void>>() {
            public ResponseEntity<Void> call() throws Exception {
                return ResponseEntity.noContent().build();
            }
        };
    }
}
