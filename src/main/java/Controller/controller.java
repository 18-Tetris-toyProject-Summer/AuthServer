package Controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

@Controller
public class controller {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String test2(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "state") String state
    //        ,RedirectAttributes redirectAttributes
            ,HttpServletResponse response
    ) throws Exception {
        String clientId = "fsL4vVVgvsoOwkPoWPO4";//애플리케이션 클라이언트 아이디값";
        //  FIXME 외부 파일에서 비밀번호 가져오도록 변경. + git ignore
        String clientSecret = "";//애플리케이션 클라이언트 시크릿값";

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
                int id;
                String nickName, email, tmp;
                JsonParser parser = new JsonParser();
                JsonElement accessElement = parser.parse(res.toString());
                access_token = accessElement.getAsJsonObject().get("access_token").getAsString();
                tmp = getUserInfo(access_token);
                JsonElement userInfoElement = parser.parse(tmp);
                id = userInfoElement.getAsJsonObject().get("response").getAsJsonObject().get("id").getAsInt();
                nickName = userInfoElement.getAsJsonObject().get("response").getAsJsonObject().get("nickname").getAsString();
                email = userInfoElement.getAsJsonObject().get("response").getAsJsonObject().get("email").getAsString();
                System.out.println(" >> "+id + ", " + nickName + ", " + email);
                //  Todo res 의 access_token 가지고 회원 정보를 요청해야함.
                access_token = createJWTToken(id, nickName, email);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        // TODO 받은 엑세스 토큰을 포워드? 해줘야 함.
        //  Cookie 에 저장하는게 옳은 것인가?
        Cookie access = new Cookie("access_token", access_token);
        //redirectAttributes.addFlashAttribute("access_token", access_token);
        response.addCookie(access);
        return "redirect:http://localhost:8080/";
    }

    private String getUserInfo(String access_token) {
        String header = "Bearer " + access_token; // Bearer 다음에 공백 추가
        System.out.println("acc : " + access_token);
        try {
            String apiURL = "https://openapi.naver.com/v1/nid/me";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", header);
            int responseCode = con.getResponseCode();
            BufferedReader br;
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
            System.out.println("res : " + res.toString());
            return res.toString();
        } catch (Exception e) {
            System.err.println(e);
            return "Err";
        }
    }

    private String createJWTToken(int id, String nickname, String email) {
        String token = null;
        DecodedJWT jwt = null;

        try {
            Date nowTime = new Date();
            long tmp = nowTime.getTime() + (long)(3600);
            Date expireDate = new Date(tmp);
            System.out.println("expireDate : " + expireDate + " / " + expireDate.getTime() + " / " + nowTime.getTime() + " / " + tmp);
            Algorithm algorithm = Algorithm.HMAC256("");
            token = JWT.create()
                    .withIssuer("auth0")
                    .withSubject(nickname)
                    .withAudience("1ilsang")
                    .withClaim("id", id)
                    .withClaim("email", email)
                    .withExpiresAt(expireDate)
                    .sign(algorithm);
        } catch (Exception e) {
            System.err.println("err: " + e);
        }
        System.out.println(token);
        return token;
    }

}
