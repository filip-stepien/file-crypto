package xyz.cursedman.filecrypto.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import xyz.cursedman.filecrypto.benchmark.BenchmarkCsvExporter;
import xyz.cursedman.filecrypto.benchmark.BenchmarkResult;
import xyz.cursedman.filecrypto.benchmark.CryptorBenchmark;
import xyz.cursedman.filecrypto.benchmark.TestFileSizes;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.utils.FileSizeFormatter;
import xyz.cursedman.filecrypto.utils.Popup;
import xyz.cursedman.filecrypto.utils.Stopwatch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class BenchmarkTabController {

    private static final int DEFAULT_REPETITIONS = 3;

    private static final int MAX_REPETITIONS = 50;

    private static final double CHART_EXPORT_SCALE = 2.0;

    @FXML
    private FileTableController fileTableController;

    @FXML
    private ProgressBarController progressBarController;

    @FXML
    private LoadingOverlayController loadingOverlayController;

    @FXML
    private VBox algorithmsContainer;

    @FXML
    private Spinner<Integer> repetitionsSpinner;

    @FXML
    private TextField testFileSizesField;

    @FXML
    private Button exportCsvButton;

    @FXML
    private Button exportChartsButton;

    @FXML
    private TabPane resultsTabPane;

    @FXML
    private TableView<BenchmarkResult> resultsTable;

    @FXML
    private BarChart<String, Number> throughputChart;

    @FXML
    private LineChart<Number, Number> timeChart;

    @FXML
    private BarChart<String, Number> overheadChart;

    private final Map<CryptorAlgorithm, CheckBox> algorithmCheckBoxes = new LinkedHashMap<>();

    private final ObservableList<BenchmarkResult> results = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        for (CryptorAlgorithm algorithm : CryptorAlgorithm.values()) {
            CheckBox checkBox = new CheckBox(algorithm.getName());
            checkBox.setSelected(true);
            algorithmCheckBoxes.put(algorithm, checkBox);
            algorithmsContainer.getChildren().add(checkBox);
        }

        repetitionsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_REPETITIONS, DEFAULT_REPETITIONS)
        );

        testFileSizesField.setText(TestFileSizes.DEFAULT);
        testFileSizesField.setPromptText(TestFileSizes.DEFAULT);

        initializeResultsTable();

        exportCsvButton.disableProperty().bind(Bindings.isEmpty(results));
        exportChartsButton.disableProperty().bind(Bindings.isEmpty(results));

        progressBarController.setButtonText("Run");
        progressBarController.setOnButtonClick(event -> runBenchmark());
    }

    private void initializeResultsTable() {
        resultsTable.setItems(results);
        resultsTable.setPlaceholder(new Label("Add files and click \"Run\" to compare algorithms"));

        addColumn("Algorithm", result -> result.algorithm().getName());
        addColumn("File", BenchmarkResult::fileName);
        addColumn("Size", result -> FileSizeFormatter.format(result.fileSize()));
        addColumn("Enc [ms]", result -> formatDecimal(result.encryptMillis()));
        addColumn("Dec [ms]", result -> formatDecimal(result.decryptMillis()));
        addColumn("Enc [MB/s]", result -> formatDecimal(result.encryptThroughput()));
        addColumn("Dec [MB/s]", result -> formatDecimal(result.decryptThroughput()));
        addColumn("Output", result -> FileSizeFormatter.format(result.outputSize()));
        addColumn("Overhead", result -> result.overheadBytes() + " B");
        addColumn("Valid", result -> result.valid() ? "yes" : "NO");
    }

    private void addColumn(String title, Function<BenchmarkResult, String> mapper) {
        TableColumn<BenchmarkResult, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        resultsTable.getColumns().add(column);
    }

    private static String formatDecimal(double value) {
        return String.format("%.2f", value);
    }

    private List<CryptorAlgorithm> getSelectedAlgorithms() {
        return algorithmCheckBoxes.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .toList();
    }

    private void finishTask(String label) {
        loadingOverlayController.setVisible(false);
        progressBarController.setButtonDisabled(false);
        progressBarController.setProgress(1);
        progressBarController.setLabel(label);
    }

    private void failTask() {
        loadingOverlayController.setVisible(false);
        progressBarController.setButtonDisabled(false);
        progressBarController.setLoading(false);
        progressBarController.setLabel("Failed");
    }

    private void runBenchmark() {
        List<File> files = fileTableController.getFiles().stream().filter(File::isFile).toList();
        List<CryptorAlgorithm> algorithms = getSelectedAlgorithms();

        if (files.isEmpty()) {
            Popup.error("No files", "No files selected", "Please add at least one file (folders are skipped).");
            return;
        }

        if (algorithms.isEmpty()) {
            Popup.error("No algorithms", "No algorithms selected", "Please select at least one algorithm.");
            return;
        }

        CryptorBenchmark benchmark = new CryptorBenchmark(repetitionsSpinner.getValue());
        Stopwatch stopwatch = new Stopwatch();

        Task<List<BenchmarkResult>> task = new Task<>() {
            @Override
            protected List<BenchmarkResult> call() throws Exception {
                stopwatch.start();
                List<BenchmarkResult> benchmarkResults = benchmark.run(files, algorithms, (done, total, message) -> {
                    updateProgress(done, total);
                    updateMessage(message + " (" + done + "/" + total + ")");
                });
                stopwatch.stop();
                return benchmarkResults;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                finishTask(
                    "Finished in " + stopwatch.getFormattedElapsed() + " · " + getValue().size() + " measurements"
                );
                showResults(getValue(), algorithms);
            }

            @Override
            protected void failed() {
                super.failed();
                failTask();
                showError("Benchmark error", "An error occurred while running the benchmark", getException());
            }
        };

        startTask(task);
    }

    private void startTask(Task<?> task) {
        task.progressProperty().addListener((obs, prev, curr) -> progressBarController.setProgress(curr.doubleValue()));
        task.messageProperty().addListener((obs, prev, curr) -> progressBarController.setLabel(curr));

        progressBarController.setLabel("Starting...");
        progressBarController.setLoading(true);
        progressBarController.setButtonDisabled(true);
        loadingOverlayController.setVisible(true);

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showResults(List<BenchmarkResult> benchmarkResults, List<CryptorAlgorithm> algorithms) {
        results.setAll(benchmarkResults);

        Map<CryptorAlgorithm, List<BenchmarkResult>> byAlgorithm = benchmarkResults.stream()
            .collect(Collectors.groupingBy(BenchmarkResult::algorithm));

        XYChart.Series<String, Number> encryptSeries = new XYChart.Series<>();
        encryptSeries.setName("Encryption");
        XYChart.Series<String, Number> decryptSeries = new XYChart.Series<>();
        decryptSeries.setName("Decryption");
        XYChart.Series<String, Number> overheadSeries = new XYChart.Series<>();
        overheadSeries.setName("Overhead");
        List<XYChart.Series<Number, Number>> timeSeries = new ArrayList<>();

        for (CryptorAlgorithm algorithm : algorithms) {
            List<BenchmarkResult> algorithmResults = byAlgorithm.getOrDefault(algorithm, List.of());

            if (algorithmResults.isEmpty()) {
                continue;
            }

            String name = algorithm.getName();
            encryptSeries.getData().add(new XYChart.Data<>(
                name, averageThroughput(algorithmResults, BenchmarkResult::encryptMillis)
            ));
            decryptSeries.getData().add(new XYChart.Data<>(
                name, averageThroughput(algorithmResults, BenchmarkResult::decryptMillis)
            ));
            overheadSeries.getData().add(new XYChart.Data<>(
                name, algorithmResults.stream().mapToLong(BenchmarkResult::overheadBytes).average().orElse(0)
            ));

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(name);
            algorithmResults.stream()
                .sorted(Comparator.comparingLong(BenchmarkResult::fileSize))
                .forEach(result -> series.getData().add(new XYChart.Data<>(
                    result.fileSize() / BenchmarkResult.BYTES_PER_MB, result.encryptMillis()
                )));
            timeSeries.add(series);
        }

        throughputChart.getData().setAll(List.of(encryptSeries, decryptSeries));
        overheadChart.getData().setAll(List.of(overheadSeries));
        timeChart.getData().setAll(timeSeries);

        if (benchmarkResults.stream().anyMatch(result -> !result.valid())) {
            Popup.error(
                "Validation failed",
                "Decrypted data does not match the original",
                "Check rows marked as \"NO\" in the Valid column."
            );
        }
    }

    private static double averageThroughput(List<BenchmarkResult> results, ToDoubleFunction<BenchmarkResult> millis) {
        long totalBytes = results.stream().mapToLong(BenchmarkResult::fileSize).sum();
        double totalMillis = results.stream().mapToDouble(millis).sum();
        return BenchmarkResult.throughput(totalBytes, totalMillis);
    }

    @FXML
    private void handleGenerateTestFiles() {
        List<Integer> sizesMb;

        try {
            sizesMb = TestFileSizes.parse(testFileSizesField.getText());
        } catch (IllegalArgumentException e) {
            Popup.error("Invalid file sizes", "Invalid test file sizes", e.getMessage());
            return;
        }

        Task<List<File>> task = new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                Path directory = Files.createTempDirectory("filecrypto-benchmark");
                Random random = new Random();
                byte[] chunk = new byte[1024 * 1024];
                List<File> files = new ArrayList<>();
                int totalMb = sizesMb.stream().mapToInt(Integer::intValue).sum();
                int writtenMb = 0;

                for (int sizeMb : sizesMb) {
                    Path path = directory.resolve("random-" + sizeMb + "MB.bin");
                    updateMessage("Generating " + path.getFileName() + "...");

                    try (OutputStream out = Files.newOutputStream(path)) {
                        for (int i = 0; i < sizeMb; i++) {
                            random.nextBytes(chunk);
                            out.write(chunk);
                            updateProgress(++writtenMb, totalMb);
                        }
                    }

                    path.toFile().deleteOnExit();
                    files.add(path.toFile());
                }

                directory.toFile().deleteOnExit();
                return files;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                finishTask("Generated " + getValue().size() + " test files");
                fileTableController.addFiles(getValue());
            }

            @Override
            protected void failed() {
                super.failed();
                failTask();
                showError("Generation error", "An error occurred while generating test files", getException());
            }
        };

        startTask(task);
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export results...");
        fileChooser.setInitialFileName("benchmark.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            BenchmarkCsvExporter.export(file.toPath(), results);
            progressBarController.setLabel("Results exported to " + file.getAbsolutePath());
        } catch (IOException | RuntimeException e) {
            showError("Export error", "Could not export results", e);
        }
    }

    @FXML
    private void handleExportCharts() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select output folder...");

        File directory = directoryChooser.showDialog(resultsTable.getScene().getWindow());

        if (directory == null) {
            return;
        }

        Map<String, XYChart<?, ?>> charts = new LinkedHashMap<>();
        charts.put("throughput.png", throughputChart);
        charts.put("time-vs-size.png", timeChart);
        charts.put("overhead.png", overheadChart);

        Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();

        try {
            for (Map.Entry<String, XYChart<?, ?>> entry : charts.entrySet()) {
                saveChart(entry.getValue(), new File(directory, entry.getKey()));
            }

            progressBarController.setLabel("Charts exported to " + directory.getAbsolutePath());
        } catch (IOException | RuntimeException e) {
            showError("Export error", "Could not export charts", e);
        } finally {
            resultsTabPane.getSelectionModel().select(selectedTab);
        }
    }

    private void saveChart(XYChart<?, ?> chart, File file) throws IOException {
        resultsTabPane.getTabs().stream()
            .filter(tab -> tab.getContent() == chart)
            .findFirst()
            .ifPresent(tab -> resultsTabPane.getSelectionModel().select(tab));

        chart.applyCss();
        chart.layout();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(CHART_EXPORT_SCALE, CHART_EXPORT_SCALE));
        WritableImage image = chart.snapshot(parameters, null);

        ImageIO.write(toBufferedImage(image), "png", file);
    }

    private static BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];

        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        return bufferedImage;
    }

    private static void showError(String title, String header, Throwable exception) {
        Popup.error(
            title,
            header,
            new VBox(
                5,
                new Label("Error log:"),
                new Label(String.valueOf(exception.getMessage())) {{
                    setStyle(
                        "-fx-font-family: 'Monospaced';" +
                        "-fx-background-color: #f0f0f0;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-padding: 4 6 4 6;"
                    );
                }}
            )
        );
    }
}
