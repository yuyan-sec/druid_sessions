import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Druid_Sessions extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public static TextArea result = new TextArea();

    public void start(Stage primaryStage) {
        AnchorPane ap = new AnchorPane();
        HBox hbox = new HBox(8);
        hbox.setPadding(new Insets(10));

        final TextField input = new TextField();
        input.setPrefWidth(330);
        input.setPromptText("请输入URL");
        input.setFocusTraversable(false);

        Button url = new Button("远程获取");
        Button file = new Button("本地获取");

        hbox.getChildren().addAll(input,url,file);

        HBox hbox2 = new HBox();
        hbox2.setPadding(new Insets(10));

        result.setPrefWidth(475);
        result.setPrefHeight(500);
        hbox2.getChildren().add(result);

        url.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String url = input.getText();
                boolean b = url.contains(".json") && url.contains("http://") | url.contains("https://");
                if (b){
                    getUrl(url);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("URL 输入错误");
                    alert.setContentText("请查看是否带有 http:// 或 https://, 是否访问的是 json 文件：\n" +
                            "http://127.0.0.1/druid/websession.json\n" +
                            "http://127.0.0.1/system/druid/websession.json\n" +
                            "http://127.0.0.1/webpage/system/druid/websession.json");
                    alert.show();
                }
            }
        });

        file.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage stage = new Stage();
                FileChooser fc = new FileChooser();
                File filePath = fc.showOpenDialog(stage);
                getFile(filePath.getAbsolutePath());
            }
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hbox,hbox2);
        ap.getChildren().addAll(vBox);

        Scene scene = new Scene(ap);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Get Druid Sessions       By: yuyan-sec");
        primaryStage.setWidth(510);
        primaryStage.setHeight(600);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private static void getUrl(String url){
        HttpResponse response = HttpRequest.get(url).send();
        parse(response.bodyText());
    }

    private static void getFile(String file){
        File fileName = new File(file);
        Long fileLength = fileName.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try {
            FileInputStream in = new FileInputStream(fileName);
            in.read(fileContent);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parse(new String(fileContent));
    }

    private static void parse(String line){
        Pattern p = Pattern.compile("[A-Za-z0-9]{32}");
        Matcher m = p.matcher(line);
        String session = "";
        while(m.find()){
            session += m.group() + "\n";
        }
        result.setText(session);
    }
}
