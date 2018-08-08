package Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Controller
public class controller {

    @RequestMapping(value = "/")
    public String test() {
        return "index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String test2(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "state") String state) throws Exception {
        String clientId = "fsL4vVVgvsoOwkPoWPO4";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "Secret-Passwd";//애플리케이션 클라이언트 시크릿값";

        String apiURL;
        apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&";
        apiURL += "client_id=" + clientId;
        apiURL += "&client_secret=" + clientSecret;
        apiURL += "&code=" + code;
        apiURL += "&state=" + state;
        String access_token = "";
        String refresh_token = "";
        System.out.println("apiURL="+apiURL);
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            System.out.print("responseCode="+responseCode);
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer res = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                res.append(inputLine);
            }
            br.close();
            if(responseCode==200) {
                System.out.println(res.toString());
                //  Todo res 의 access_token 가지고 회원 정보를 요청해야함과 Redis 갱신 및 만료 체크.
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return "redirect:http://localhost:8080/";
    }
}
