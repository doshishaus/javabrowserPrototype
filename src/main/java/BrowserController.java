import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TabPane.TabClosingPolicy;

public class BrowserController {

    @FXML
    private TabPane tabPane;
    @FXML
    private TextField urlField;
    @FXML
    private ProgressBar progressBar;

    public void initialize() {
        handleNewTabWithUrl("https://www.google.com");
        tabPane.getTabs().addListener((ListChangeListener<Tab>) c -> adjustTabWidth());
        adjustTabWidth();
    }

    @FXML
    private void handleNewTab() {
        handleNewTabWithUrl("https://www.google.com");
    }

    private void handleNewTabWithUrl(String url) {
        Tab tab = new Tab("Loading...");
        WebView webView = new WebView();
        initializeWebView(webView, tab);
        webView.getEngine().load(url);
        tab.setContent(webView);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    @FXML
    private void handleGo() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            WebView webView = (WebView) selectedTab.getContent();
            String url = urlField.getText();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url; // URLがhttpまたはhttpsで始まらない場合、httpを付加
            }
            webView.getEngine().load(url);
        }
    }

    @FXML
    private void handleGoBack() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            WebView webView = (WebView) selectedTab.getContent();
            if (webView.getEngine().getHistory().getCurrentIndex() > 0) {
                webView.getEngine().getHistory().go(-1);
            }
        }
    }

    @FXML
    private void handleGoForward() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            WebView webView = (WebView) selectedTab.getContent();
            if (webView.getEngine().getHistory()
                    .getCurrentIndex() < webView.getEngine().getHistory().getEntries().size() - 1) {
                webView.getEngine().getHistory().go(1);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            WebView webView = (WebView) selectedTab.getContent();
            webView.getEngine().reload(); // ページの再読み込みを実行
        }
    }

    // WebViewの初期化を行うメソッド
    private void initializeWebView(WebView webView, Tab tab) {
        WebEngine engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newState) -> {
            if (newState == Worker.State.RUNNING) {
                progressBar.setVisible(true);
            } else if (newState == Worker.State.SUCCEEDED || newState == Worker.State.FAILED) {
                progressBar.setVisible(false);
            }
        });

        engine.getLoadWorker().workDoneProperty().addListener((observable, oldValue, newValue) -> {
            progressBar.setProgress(newValue.doubleValue() / 100.0);
        });

        engine.titleProperty().addListener((observable, oldValue, newValue) -> {
            tab.setText(newValue); // タブのタイトルを更新
        });
    }

    private void webViewNavigation(int direction) {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            WebView webView = (WebView) selectedTab.getContent();
            if (direction < 0 && webView.getEngine().getHistory().getCurrentIndex() > 0) {
                webView.getEngine().getHistory().go(direction);
            } else if (direction > 0 && webView.getEngine().getHistory()
                    .getCurrentIndex() < webView.getEngine().getHistory().getEntries().size() - 1) {
                webView.getEngine().getHistory().go(direction);
            }
        }
    }

    private void adjustTabWidth() {
        double tabCount = tabPane.getTabs().size();
        double newWidth = Math.max(tabPane.getWidth() / tabCount, 100); // 最小幅を100とする
        for (Tab tab : tabPane.getTabs()) {
            tab.setStyle("-fx-tab-min-width: " + newWidth + "px; -fx-tab-max-width: " + newWidth + "px;");
        }
    }
}
