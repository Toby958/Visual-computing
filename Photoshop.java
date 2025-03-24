//Tobias Jeffries 2215368
//Jack Jones 2211670

package com.example.visulacomputing;
        import javafx.application.Application;
        import javafx.scene.Scene;
        import javafx.scene.control.Separator;
        import javafx.scene.control.CheckBox;
        import javafx.scene.control.Label;
        import javafx.scene.control.Slider;
        import javafx.scene.image.Image;
        import javafx.scene.image.ImageView;
        import javafx.scene.image.PixelWriter;
        import javafx.scene.layout.VBox;
        import javafx.stage.Stage;
        import javafx.scene.paint.Color;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;

public class Photoshop extends Application {
    private Image originalImage;
    private ImageView imageView;
    public static void main(String[] args) {
        launch(args);
    }
    private final double[] gammaLUT = new double[256];


    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mark's CS-256 application");

        // Create an ImageView
        imageView = new ImageView();

        // Load the image from a file
        try {
            originalImage = new Image(new FileInputStream("raytrace.jpg"));
            imageView.setImage(originalImage);
        } catch (FileNotFoundException e) {
            System.out.println(">>>The image could not be located in directory: "+System.getProperty("user.dir")+"<<<");
            System.exit(-1);
        }



        // Create a Slider for gamma correction
        Slider gammaSlider = new Slider(0.1, 3.0, 1.0);

        // Create a Label to display the current gamma value
        Label gammaLabel = new Label("Gamma: " + gammaSlider.getValue());



        // Add a listener to update the image with gamma correction when the slider is changed
        gammaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double gammaValue = newValue.doubleValue();
            gammaLabel.setText("Gamma: " + gammaValue);

            setGammaLUT(gammaValue);

            // Apply gamma correction to the image and update the ImageView
            Image correctedImage = applyGammaCorrection(originalImage);
            imageView.setImage(correctedImage);
        });



        //Setting the dimensions for resize sliders
        Slider resizeSlider = new Slider(0.1, 2.0, 1.0);
        Label resizeLabel = new Label("Resize: " + resizeSlider.getValue());

        //Checkbox for interpolation method
        CheckBox nn = new CheckBox("Nearest Neighbour Interpolation");
        nn.setSelected(false);

        // checkbox for laplacian cross correlation
        CheckBox mm = new CheckBox("Laplacian Cross Correlation ");
        mm.setSelected(false);



        // Add a listener to update the image with resizing when the slider is changed
        resizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double scale = newValue.doubleValue();
            resizeLabel.setText("Resize: " + scale);

            // Check if the checkbox is selected
            if (nn.isSelected()) {
                // Perform resizing using Nearest Neighbour Interpolation
                Image resizedImage = resizeNeighbour(originalImage, scale);
                imageView.setImage(resizedImage);
            } else {
                // If the checkbox is not selected carry out Bilinear interpolation
                Image resizedImage = resizeBilinear(originalImage, scale);
                imageView.setImage(resizedImage);
            }

            // Check if the checkbox is selected
            if (mm.isSelected()) {
                // Performs Laplacian Cross Correlation
                Image correlatedImage = applyCrossCorrelation(imageView.getImage());
                imageView.setImage(correlatedImage);
            }
        });


        //Spacings for the sliders and checkboxes
        Separator separator1 = new Separator();
        Separator separator2 = new Separator();
        Separator separator3 = new Separator();
        Separator separator4 = new Separator();

        // Create a VBox to hold the components
        VBox vbox = new VBox(gammaSlider, gammaLabel, separator1, resizeSlider, resizeLabel, separator2, nn, separator3, mm, separator4, imageView);

        // Create a scene with the VBox
        Scene scene = new Scene(vbox, 400, 600);

        // Set the scene to the stage
        primaryStage.setScene(scene);
        // Show the stage
        primaryStage.show();
    }



    //Look up table to calculate gamma corrected values
    private void setGammaLUT(double gamma) {
        for (int i = 0; i < 256; i++) {
            gammaLUT[i] = Math.pow((double) i/255.0, 1.0/gamma);
        }
    }

    //Gamma correction function
    private Image applyGammaCorrection(Image originalImage) {
        int newWidth = (int) originalImage.getWidth();
        int newHeight = (int) originalImage.getHeight();

        // Create a new WritableImage
        javafx.scene.image.WritableImage gammaCorrectedImage = new javafx.scene.image.WritableImage(newWidth, newHeight);
        PixelWriter writableImage = gammaCorrectedImage.getPixelWriter();

        //Variable to hold colour of a pixel
        Color colour;

        //This loop retrieves the colour of a pixel at a position then applies gamma correction to it
        for (int j=0; j<newHeight; j++)
            for (int i=0; i<newWidth; i++) {
                colour = originalImage.getPixelReader().getColor(i, j);
                double r = gammaLUT[(int)(colour.getRed()*255.0)];
                double g = gammaLUT[(int)(colour.getGreen()*255.0)];
                double b = gammaLUT[(int)(colour.getBlue()*255.0)];
                colour=Color.color(r, g ,b);
                writableImage.setColor(i, j, colour);
            }

        return gammaCorrectedImage;

    }



    //Preforms linear interpolation
    private double lerp(double v1, double v2, double frac) {
        return v1 + (v2 - v1) * frac;
    }

    //Calculating each colour using the four coordinates
    private Color bilinearCol(Color c00, Color c10, Color c01, Color c11, double fracX, double fracY) {
        double red = lerp(lerp(c00.getRed(), c10.getRed(), fracX), lerp(c01.getRed(), c11.getRed(), fracX), fracY);
        double green = lerp(lerp(c00.getGreen(), c10.getGreen(), fracX), lerp(c01.getGreen(), c11.getGreen(), fracX), fracY);
        double blue = lerp(lerp(c00.getBlue(), c10.getBlue(), fracX), lerp(c01.getBlue(), c11.getBlue(), fracX), fracY);
        double opacity = lerp(lerp(c00.getOpacity(), c10.getOpacity(), fracX), lerp(c01.getOpacity(), c11.getOpacity(), fracX), fracY);
        return new Color(red, green, blue, opacity);
    }

    //Bilinear Interpolation
    private Image resizeBilinear(Image originalImage, double scale) {
        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);

        // Create a new WritableImage
        javafx.scene.image.WritableImage resizedImage = new javafx.scene.image.WritableImage(newWidth, newHeight);
        PixelWriter writeableImage = resizedImage.getPixelWriter();

        //Variable to hold colour of a pixel
        Color colour;

        //This loop finds the current colour of a pixel
        for (int j = 0; j < newHeight; j++) {
            for (int i = 0; i < newWidth; i++) {
                //Calculates corresponding coordinates of the resized image
                double x = (double) i / newWidth * (originalImage.getWidth() - 1);
                double y = (double) j / newHeight * (originalImage.getHeight() - 1);

                //Gets the four points for bilinear interpolation
                int x0 = (int) x;
                int y0 = (int) y;
                int x1 = Math.min(x0 + 1, (int) originalImage.getWidth() - 1);
                int y1 = Math.min(y0 + 1, (int) originalImage.getHeight() - 1);

                double fracX = x - x0;
                double fracY = y - y0;

                //retries the colours of the four the nearest pixels in original iamge
                Color c00 = originalImage.getPixelReader().getColor(x0, y0);
                Color c10 = originalImage.getPixelReader().getColor(x1, y0);
                Color c01 = originalImage.getPixelReader().getColor(x0, y1);
                Color c11 = originalImage.getPixelReader().getColor(x1, y1);

                // Takes the colours and interpolates them between x then y using the new scales
                colour = bilinearCol(c00, c10, c01, c11, fracX, fracY);
                writeableImage.setColor(i, j, colour);
            }
        }

        return resizedImage;
    }



    //Nearest neighbour interpolation
    private Image resizeNeighbour(Image originalImage, double scale) {
        int newWidth = (int) (originalImage.getWidth()*scale);
        int newHeight = (int) (originalImage.getHeight()*scale);

        // Create a new WritableImage
        javafx.scene.image.WritableImage resizedImage = new javafx.scene.image.WritableImage(newWidth, newHeight);
        PixelWriter writeableImage = resizedImage.getPixelWriter();

        //Variable to hold colour of a pixel
        Color colour;

        //Loop to get the current colour of a pixel
        for (int j = 0; j < newHeight; j++)
            for (int i = 0; i < newWidth; i++) {

                //Gets a pixel at points i and j of the new resized image
                int x = (int) (originalImage.getWidth()* (double) i / (double) newWidth);
                int y = (int) (originalImage.getHeight()* (double) j / (double) newHeight);

                //Gets the colour from the original image and sets it as the resized image
                colour = originalImage.getPixelReader().getColor(x,y);
                writeableImage.setColor(i, j, colour);
            }

        return resizedImage;
    }



    //Laplacian Filter used for cross correlation
    private static final int[][] laplacianFilter = {
            {-4,-1, 0,-1,-4},
            {-1, 2, 3, 2,-1},
            { 0, 3, 4, 3, 0},
            {-1, 2, 3, 2,-1},
            {-4,-1, 0,-1,-4},
    };

    //Laplacian cross correlation
    public static Image applyCrossCorrelation(Image originalImage) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();



        //Sets an array and defines minand max values
        double[][] normalizedImage = new double[width][height];
        double maxIntensity = Double.MIN_VALUE;
        double minIntensity = Double.MAX_VALUE;

        // Create a writable image
        javafx.scene.image.WritableImage correlatedImage = new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter writeableImage = correlatedImage.getPixelWriter();

        // Perform cross-correlation using weighted sum for each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double weightedSum = calculateWeightedSum(originalImage, width, height, x, y);
                normalizedImage[x][y] = weightedSum;
                maxIntensity = Math.max(maxIntensity, weightedSum);
                minIntensity = Math.min(minIntensity, weightedSum);
            }
        }

        // Normalization and setting pixel values
        double range = maxIntensity - minIntensity;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double normalizedValue = (normalizedImage[x][y] - minIntensity) / range;
                writeableImage.setColor(x, y, Color.gray(normalizedValue));

            }
        }


        return correlatedImage;
    }

    // Helper function to calculate weighted sum
    private static double calculateWeightedSum(Image originalImage, int width, int height, int x, int y) {

        //Sets variable sum
        double weightedSum = 0;

        // Gets the filter size
        int filterSize = laplacianFilter.length;
        int halfFilterSize = filterSize / 2;

        //Loop goes of each value of the laplacian filter
        for (int j = 0; j < filterSize; j++) {
            for (int i = 0; i < filterSize; i++) {

                //Calculating neighbouring pixel based on the half size to make sure it's in the boundary
                int pixelX = x - halfFilterSize + i;
                int pixelY = y - halfFilterSize + j;

                //Checking it's in the bounds of the image and getting the greyscale brightness of each pixel
                if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) {
                    weightedSum += laplacianFilter[j][i] * originalImage.getPixelReader().getColor(pixelX, pixelY).getBrightness();
                }
            }
        }
        return weightedSum;
    }




}







