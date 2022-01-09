import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** Starter code for Project 4: wireless router signal strength.
 */
public class Proj1 {

   public static void main(String[] args) throws IOException {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
   
      final double EPS = .0001;
      final String PROMPT1 = "Enter name of grid data file: ";
      final String ERROR = "ERROR: invalid input file";
   
      Scanner kb = new Scanner(System.in);
      System.out.print(PROMPT1);
      String name = kb.nextLine();
      
      FileInputStream infile = new FileInputStream(name);
      Scanner scanFile = new Scanner(infile);
      double size = scanFile.nextDouble();
      int rows = scanFile.nextInt();
      int cols = scanFile.nextInt();
      scanFile.nextLine(); // skip past end of line character 
      
      FileOutputStream outstream = new FileOutputStream("signals.txt");
      PrintWriter outfile = new PrintWriter(outstream);  
      
      Cell[][] grid = new Cell[rows][cols];
      Cell[][] old = new Cell[rows][cols];
      initialize(grid);
      initialize(old); 
               
      int routerRow;
      int routerCol;
      final double ROUTER = 23;
      
            
      read(grid, scanFile);
     
      if (! isValid(grid)) {
         System.out.println(ERROR);
      } else { 
         // keep processing 
         System.out.print("Enter router row and column: ");
         routerRow = kb.nextInt();
         routerCol = kb.nextInt();
         grid[routerRow][routerCol].setSignal(ROUTER);
                 
         setAllDirections(grid, routerRow, routerCol);
         setAllDistances(grid, routerRow, routerCol, size);
                
                  
         while (! equivalent(grid, old, EPS)) {
            copy(grid, old);     
            iterate(grid, old, routerRow, routerCol);  
            printAll(grid, outfile);   
            outfile.println();  // blank link separator
         }
      }
      
      double minSignal = findMinSignal(grid);
      System.out.println("minimum signal strength: " + minSignal + 
                     " occurs in these cells: ");
      printMinCellCoordinates(grid, minSignal);
      
      outstream.flush();
      outfile.close();
   }
   
   /** Set the direction from the router position to every other cell
    *  in the grid. (Do not change the direction of the router cell.)
    *  @param grid the grid of cells to manage
    *  @param routerRow the row position of the router cell in the grid
    *  @param routerCol the column position of the router cell in the grid
    */
   public static void setAllDirections(Cell[][] grid, int routerRow, 
                                       int routerCol) {
   // Declare variables
      int rows = grid.length; 
      int cols = grid[1].length;
      int i;
      int j;
      String dir;
      
      // Iterate over each row and each column of grid
      for (i = 0; i < rows; ++i) { 
         for (j = 0; j < cols; ++j) {
            if ((i != routerRow) || (j != routerCol)) {
               // Determine direction at particular cell.
               dir = direction(routerRow,routerCol, i, j); 
               grid[i][j].setDirection(dir); // Assign direction to Cell object.
               
            }
         }            
      }
   
   }
   
   /** Set the distance from the router position to every other cell
    *  in the grid. (Do not change the distance of the router cell.)
    *  @param grid the grid of cells to manage
    *  @param routerRow the row position of the router cell in the grid
    *  @param routerCol the column position of the router cell in the grid
    *  @param size the size of each cell
    */
   public static void setAllDistances(Cell[][] grid, int routerRow,
                                       int routerCol, double size) {
   //THIS METHOD IS COMPLETE -DO NOT CHANGE IT
      for (int i = 0; i < grid.length; i++) {
         for (int j = 0; j < grid[0].length; j++) {
            if (! (i == routerRow && j == routerCol)) {
               grid[i][j].setDistance(size * Math.sqrt(Math.pow(routerRow - i,
                        2) + Math.pow(routerCol - j, 2)));
            }
         }
      }
   }

   
   /** Iterate over the grid, updating the signal strength and
    *  attenuation rate of each cell based on the old values of
    *  the relevant neighbor cells.
    *  @param current the updated values of each cell
    *  @param previous the old values of each cell
    *  @param routerRow the row position of the router's cell
    *  @param routerCol the column position of the router's cell
    */         
   public static void iterate(Cell[][] current, Cell[][] previous,  
                              int routerRow, int routerCol) {
   // Declare variables
      final int ROWS = current.length;
      final int COLS = current[1].length;
      double signal;
      double dist;
      double dummy_signal;
      int dummy_rate;
      int i;
      int j;
      
      Cell cell;
                  
      for (i = 0; i < ROWS; ++i) { //Iterate over rows
         for (j = 0; j < COLS; ++j) { //Iterate over columns
            if ((i != routerRow) || (j != routerCol)) { // Check that we are not at router cell.
               cell = current[i][j];
               
               dist = cell.getDistance(); 
               dummy_rate = cell.getRate(); //store current rate in dummy variable
               dummy_signal = cell.getSignal(); //store current strength in dummy variable
               
               signal = current[routerRow][routerCol].getSignal() - fspl(dist, 5); // signalstrength w/o atten_rate.
               cell.setRate(attenRate(previous, i, j)); // Calculate and set attenRate for current
               cell.setSignal(signal - cell.getRate()); // set Signal Strength for current
               
               previous[i][j].setRate(dummy_rate); // Set attenrate for previous
               previous[i][j].setSignal(dummy_signal); //Set signal strength for previous
               
            }
         }
      }
   
      
   }
   
   /** Calculate the signal transmission free space path loss (FSPL).
    *  @param distance the distance from the source to the receiver
    *  @param frequency the frequency of the transmission
    *  @return the fspl ratio
    */
   public static double fspl(double distance, double frequency) {
      // Declare variable and calculate fspl
      double fspl;
      fspl = 20 * Math.log(distance) / Math.log(10) + 20 * Math.log(frequency) / Math.log(10) + 92.45;
      return fspl;
      
   }
   
   /** Return the opposite direction given the direction of the current cell
    *  from the router cell.
    *  @param cell the current cell
    *  @return the opposite direction
    */
   public static String oppDirection(Cell cell) { 
      if (cell.getDirection().length() == 1) { // Check length of cell's direction.
         String dir = cell.getDirection();   // It is 1 if it is a single direction like "N"
         
         switch (dir) {
            case "N":
               return "S";
            case "S":
               return "N";
            case "E":
               return "W";
            case "W":
               return "E";
            default:
               return null;
         }
         
      } else if (cell.getDirection().length() == 2) { //Length = 2 when it is a direction like
         String dir = cell.getDirection();  // "NW".
         
         switch (dir) {
            case "NE":
               return "SW";
            case "NW":
               return "SE";
            case "SE":
               return "NW";
            case "SW":
               return "NE";
            default:
               return null;
         
         }
      }
      return "None";
      
   }
   /** Calculate the attenuation rate of a cell based on the
    *  attenuation of its relevant neighbor(s).
    *  @param prev the grid of cells from prior iteration
    *  @param row the row of the current cell
    *  @param col the column of the current cell
    *  @return the new attenuation rate of that cell
    */
   
   public static int attenRate(Cell[][] prev, int row, int col) {
     
      Cell cell = prev[row][col];
      String dir = oppDirection(cell); //gets opposite direction to 
      
      int northRate;
      int southRate;
      int eastRate;
      int westRate;
    
      if (dir.length() == 1) { // This will only happen if cell is non-diagonal.
         switch (dir) {
         
            case ("N"):
               return attenuation(cell.getNorth()) + prev[row-1][col].getRate();
            
            case ("S"):
               return attenuation(cell.getSouth()) + prev[row+1][col].getRate();
            
            case ("E"):
               return attenuation(cell.getEast()) + prev[row][col+1].getRate();
            
            case ("W"):
               return attenuation(cell.getWest()) + prev[row][col-1].getRate();
         }   
      }
      
      else if (dir.length() == 2) { // This will only happen if cell is diagonal.
         // Then, we calculate the attenuation rate coming from each neighbour.         
         // Return max. (rate of the neighbours in each of the two opp. direction).
         switch (dir) {
                  
            case ("NE"):
               northRate = attenuation(cell.getNorth()) + prev[row-1][col].getRate();
               eastRate = attenuation(cell.getEast()) + prev[row][col+1].getRate();
               return Math.max(northRate, eastRate);
            
            case ("NW"):
               northRate = attenuation(cell.getNorth()) + prev[row-1][col].getRate();
               westRate = attenuation(cell.getWest()) + prev[row][col-1].getRate();
               return Math.max(northRate, westRate);
            
            case ("SE"):
               southRate = attenuation(cell.getSouth()) + prev[row+1][col].getRate();
               eastRate = attenuation(cell.getEast()) + prev[row][col+1].getRate(); 
               return Math.max(southRate, eastRate);
                           
            case ("SW"):
               southRate = attenuation(cell.getSouth()) + prev[row+1][col].getRate();
               westRate = attenuation(cell.getWest()) + prev[row][col-1].getRate();
               return Math.max(southRate, westRate);
         }   
      }

      
      
      
      return 0;  
   }
    
   
   /** Find the direction between the router cell and the current cell.
    *  @param r0 the router row
    *  @param c0 the router column
    *  @param r1 the current cell row
    *  @param c1 the current cell column
    *  @return a string direction heading (N, E, S, W, NE, SE, SW, NW)
    */
   public static String direction(int r0, int c0, int r1, int c1) {
   //THIS METHOD IS COMPLETE -DO NOT CHANGE IT
      int rDelta = Math.abs(r0 - r1);
      int cDelta = Math.abs(c0 - c1);
      
      if (rDelta > cDelta) {
         if (r1 < r0) {
            return "N";
         } else if (r1 > r0) {
            return "S";
         }
      }
      if (rDelta < cDelta) {
         if (c1 > c0) {
            return "E";
         } else if (c1 < c0) {
            return "W";
         }
      }
      
      // rDelta == cDelta -> on a diagonal
      if (r1 < r0 && c1 > c0) {
         return "NE";
      } else if (r1 < r0 && c1 < c0) {
         return "NW";
      } else if (r1 > r0 && c1 < c0) {
         return "SW";
      } else {
         return "SE";
      }
   }


   /** Determine if the corresponding cells in the two grids of the same size
    *  have the same signal value, to a specified precision.
    *  @param grid1 the first grid
    *  @param grid2 the second grid
    *  @param epsilon the difference cutoff that makes two values "equivalent"
    *  @return true if the grids are the same sizes, and the signal values 
    *   are all within (<=) epsilon of each other; false otherwise
    */
   public static boolean equivalent(Cell[][] grid1, Cell[][] grid2, 
                                    double epsilon) {
   // TODO: write the body of this method
      boolean isEquivalent = true;
      int i;
      int j;
      
      for (i = 0; i<grid1.length; ++i) {
         for (j = 0; j<grid1[i].length; ++j) {
            isEquivalent = (Math.abs(grid1[i][j].getSignal() - grid2[i][j].getSignal()) <= epsilon) ? true:false;
            if (isEquivalent == false) {
               return isEquivalent;
            }
         }
      }
      return isEquivalent;
   }


   /**
    * Read a grid from a plain text file using a Scanner that has
    * already advanced past the first line. This method assumes the 
    * specified file exists. Each subsequent line provides the wall
    * information for the cells in a single row, using a 4-character 
    * string in NESW (north-east-south-west) order for each cell. 
    * @param grid is the grid whose Cells must be updated with the input data
    * @param scnr is the Scanner to use to read the rest of the file
    * @throws IOException if file can not be read
    */
   public static void read(Cell[][] grid, Scanner scnr) throws IOException {
   // TODO: write the body of this method  
      
      int i; // Initialising loop variables
      int j;
      final int ROW = grid.length; // Determining rows, columns in grid.
      final int COL = grid[1].length;
      
      for (i = 0; i < ROW; ++i) { // Looping through each line
         for (j = 0; j < COL; ++j) { // Looping through each column
            grid[i][j].setWalls(scnr.next()); //Assigning string to the array
         }
         if (i != (ROW - 1)) {
            scnr.nextLine(); //skipping to next line, given it exists.
         }
         else {
            break; //if next line does not exist, then exit the loop.
         }
      }
   }

   /**
    * Validate the cells of a maze as being consistent with respect
    * to neighboring internal walls. For example, suppose some cell
    * C has an east wall with material 'b' for brick. Then for the 
    * maze to be valid, the cell to C's east must have a west wall
    * that is also 'b' for brick. (This method does not need to check
    * external walls.) 
    * @param grid the grid to check
    * @return true if valid (consistent), false otherwise
    */
   public static boolean isValid(Cell[][] grid) {
      // Declare variables
      final int ROWS = grid.length;
      final int COLS = grid[0].length;
      int i;
      int j;
      boolean isOk = true;
      
      for (i = 0; i < ROWS; ++i) {//Checking if rows are valid by iterating row-wise over cells (i.e. leftwards).
         for (j = 0; j < COLS - 1; ++j) {
            isOk = (grid[i][j].getEast() == grid[i][j+1].getWest()); // Check if walls match.
            if (isOk == false) { //Return out of function if walls ever don't match.
               return isOk;
            }
         }
      }
      
      for (j = 0; j < COLS; ++j) {//Checking if columns are valid by iterating column-wise over cells (i.e. downwards).
         for (i = 0; i < ROWS - 1; ++i) {
            isOk = (grid[i][j].getSouth() == grid[i+1][j].getNorth()); // Check if walls match.
            if (isOk == false) { //Return out of function if walls ever don't match.
               return isOk;
            }
         }
      }
      return isOk; 

      
   }

   /** Find the minimum cell signal strength.
    *  @param grid the grid of cells to search
    *  @return the minimum signal value
    */
   public static double findMinSignal(Cell[][] grid) {
      // declare variables.
      final int ROWS = grid.length;
      final int COLS = grid[1].length;
      int i;
      int j;
      double minSignal = grid[0][0].getSignal();
      
      for (i = 0; i < ROWS; ++i) { //Iterate over rows
         for (j = 0; j < COLS; ++j) { //Iterate over columns
            minSignal = (grid[i][j].getSignal() < minSignal) ? grid[i][j].getSignal(): minSignal; //Check if signal at current cell < minSignal. Assign to minSignal if it is.
         
         }
      }
      return minSignal;
   }
   
   /** Print the coordinates of cells with <= the minimum signal strength,
    *  one per line in (i, j) format, in row-column order.
    *  @param grid the collection of cells
    *  @param minSignal the minimum signal strength
    */
   public static void printMinCellCoordinates(Cell[][] grid, double minSignal) {
   // Declare variables.
      final int ROWS = grid.length;
      final int COLS = grid[1].length;
      int i;
      int j;
      
       for (i = 0; i < ROWS; ++i) { //Iterate over rows
         for (j = 0; j < COLS; ++j) { //Iterate over columns
            if (grid[i][j].getSignal() <= minSignal) { // Check if signal at current cell < minSignal
            System.out.printf("%d, %d\n",i,j); // Print if above condition is true.
            }
         }
       }

   
   }

   /** Get the attenuation rate of a wall material.
    *  @param wall the material type 
    *  @return the attenuation rating
    */
   public static int attenuation(char wall) {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
      switch (wall) {
         case 'b': 
            return 22;
         case 'c': 
            return 6;
         case 'd': 
            return 4;
         case 'g': 
            return 20;
         case 'w': 
            return 6;
         case 'n': 
            return 0;
         default:
            System.out.println("ERROR: invalid wall type");
      }
      return -1;
   }


   /** Create a copy of a grid by copying the contents of each
    *  Cell in an original grid to a copy grid. Note that we use the 
    *  makeCopy method in the Cell class for this to work correctly.
    *  @param from the original grid
    *  @param to the copy grid
    */
   public static void copy(Cell[][] from, Cell[][] to) { 
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT 
      for (int i = 0; i < from.length; i++) {
         for (int j = 0; j < from[0].length; j++) {
            to[i][j] = from[i][j].makeCopy();
         }
      }
   }
   
   /** Initialize a grid to contain a bunch of new Cell objects.
    *  @param grid the array to initialize
    */
   public static void initialize(Cell[][] grid) {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
      for (int i = 0; i < grid.length; i++) {
         for (int j = 0; j < grid[0].length; j++) {
            grid[i][j] = new Cell();
         }
      }
   }
   
   /** Display the computed values of a grid (signal strenth, direction,
    *  attenuation rate, and distance to the provided output destination,
    *  using the format provided by the toString method in the Cell class.
    *  @param grid the signal grid to display
    *  @param pout the output location
    */
   public static void printAll(Cell[][] grid, PrintWriter pout) {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
      for (int i = 0; i < grid.length; i++) {
         for (int j = 0; j < grid[0].length; j++) {
            pout.print(grid[i][j].toString() + " ");
         }
         pout.println();
      }
   }
}
