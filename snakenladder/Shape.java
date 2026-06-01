package snakenladder;

import java.util.Random;

//In Shape.java - Adding piece rotation
public class Shape {
 enum Tetrominoes {
     NoShape, ZShape, SShape, LineShape,
     TShape, SquareShape, LShape, MirroredLShape
 }

 private Tetrominoes pieceShape;
 private int[][] coords;

 public Shape() {
     coords = new int[4][2];
     setShape(Tetrominoes.NoShape);
 }

 public void setShape(Tetrominoes shape) {
     int[][][] coordsTable = new int[][][]{
             {{0, 0}, {0, 0}, {0, 0}, {0, 0}},      // NoShape
             {{-1, -1}, {0, -1}, {0, 0}, {1, 0}},   // ZShape
             {{-1, 0}, {0, 0}, {0, -1}, {1, -1}},   // SShape
             {{-2, 0}, {-1, 0}, {0, 0}, {1, 0}},    // LineShape
             {{-1, 0}, {0, 0}, {1, 0}, {0, 1}},     // TShape
             {{0, 0}, {1, 0}, {0, 1}, {1, 1}},      // SquareShape
             {{-1, 0}, {0, 0}, {1, 0}, {1, 1}},     // LShape
             {{-1, 0}, {0, 0}, {1, 0}, {-1, 1}}     // MirroredLShape
     };

     for (int i = 0; i < 4; i++) {
         coords[i][0] = coordsTable[shape.ordinal()][i][0];
         coords[i][1] = coordsTable[shape.ordinal()][i][1];
     }
     pieceShape = shape;
 }

 public void setRandomShape() {
     Random r = new Random();
     int x = 1 + r.nextInt(7);
     setShape(Tetrominoes.values()[x]);
 }

 public void rotate() {
     if (pieceShape == Tetrominoes.SquareShape) {
         return; // Square doesn't rotate
     }

     // Simple 90-degree clockwise rotation around (0,0)
     int[][] newCoords = new int[4][2];
     for (int i = 0; i < 4; i++) {
         newCoords[i][0] = -coords[i][1];
         newCoords[i][1] = coords[i][0];
     }
     coords = newCoords;
 }

 public void rotateBack() {
     if (pieceShape == Tetrominoes.SquareShape) {
         return; // Square doesn't rotate
     }

     // Simple 90-degree counterclockwise rotation around (0,0)
     int[][] newCoords = new int[4][2];
     for (int i = 0; i < 4; i++) {
         newCoords[i][0] = coords[i][1];
         newCoords[i][1] = -coords[i][0];
     }
     coords = newCoords;
 }

 public int x(int index) { return coords[index][0]; }
 public int y(int index) { return coords[index][1]; }
 public Tetrominoes getShape() { return pieceShape; }
}
