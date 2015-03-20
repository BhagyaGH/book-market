/* Name : B.G.Hettige
 * Index No : 110216D
 */

/* Data Structures & Algorithms
 * Mini Project
 * **Online Book Market** 
 */

package book_market;

import java.sql.Timestamp; //Timestamp is used for the date & time format
import java.io.*; //For reading the input file
import java.util.*; //StringTokenizer used for tokenizing the String

//RatesEntry is used to record the recent rating lists in the Book & the Vendor
class RatesEntry {
    private Timestamp time;
    private int rate;
    private String user;
    
    public RatesEntry(Timestamp t, int r, String u) {
        time = t;
        rate = r;
        user = u;
    }
    //Returns the date & time of the rating 
    public Timestamp getTime() {
        return time;
    }
    //Returns the rate given by the particular user
    public int getRate() {
        return rate;
    }
    //Returns the name of the user
    public String getUser() {
        return user;
    }
}

//Book class holds the information of each book in the online book market 
class Book {
    private String name;
    private RatesEntry rates[]; //The recent rates are recorded in this 5 element rates array
    private double overallRate;
    private Hashtable userRegister; //Hashtable to hold the list of users to prevent duplicates in users[]
    private UserKey users[]; //Holds UserKey's with previous rating records of the particular book
    private int index; //To iterate through the users array
    private Max_Heap vendors; //Max_Heap to hold the Vendor objects to give the top rated vendors by heap sorting
    private Hashtable venRegister; //This is a register of the Vendors to prevent duplicates
    private Book_Market market; //The book market in which the book is belonged to 
    
    //Constructor for the Book which takes the name of the book and the book market
    Book(String name, Book_Market market) {
        this.name = name;
        this.market = market;
        
        rates = new RatesEntry[5]; //RatesEntry table is of size 5
        userRegister = new Hashtable(100); //I have given typical sizes for the hashtables & the max_heap
        users = new UserKey[100];
        vendors = new Max_Heap(100);
        venRegister = new Hashtable(100);
        index = 0;
   }
    
    //Entering a new rating record to the database    
   public void enterNewRate(Timestamp time, int rate, String user, Vendor vendor) {
        //This inserts the record to the "rates" array if it's recent        
        insertRate(new RatesEntry(time, rate, user));
        
        UserKey userKey; //UserKey to hold the previous rating records of the user
        //If the user rates the book for the first time then enter the user into the hashtable
        if(!userRegister.isExist(user)) { //Checks if the user is not in the Register
            userKey = new UserKey(user);
            userRegister.insert(userKey, user);
            users[index] = userKey; //Insert into the users array
            index++;
        }
        //If the user has rated before then simply load the previous UserKey for update        
        else {
            userKey = (UserKey)userRegister.search(user);
        }
        //Update the rating records in the hashtable accordingly - k value & rate        
        userKey.increment_k();
        userKey.enter_r(rate);
        
        //Checks the vendor register to see the existance of the vendor
        if(!venRegister.isExist(vendor.getName())) {
            venRegister.insert(vendor, vendor.getName()); //Insert the vendor into the register
            vendors.insert(vendor); //Insert the vendor into the vendors max_heap
        }        
    }
    
   //This method inserts the rate into the recent "rates" array (with modified Insertion Sort)
    public void insertRate(RatesEntry r) {
        int i=0;
        //Checks whether a free space is available in the array
        while(i<5 && rates[i] != null) {
            i++;
        }
        if(i<5) { //If so insert the rate using insertion sort algorithm backward
            rates[i] = r;
            int j=i-1;
            while(j>=0 && rates[j].getTime().after(r.getTime())) {
                rates[j+1] = rates[j];
                j--;
            }
            rates[j+1] = r;
        }
        //If the array is full;
        //Then with the oldest rate to see whether the rate is recent
        //If so insert the rate using insertion sort algorithm forward        
        else if(rates[0] != null && rates[0].getTime().before(r.getTime())) {
             rates[0] = r;
             int j=1;
             while(j<5 && rates[j].getTime().before(r.getTime())) {
                 rates[j-1] = rates[j];
                 j++;
             }
             rates[j-1] = r;
        }   
    }
    
    //Returns the name of the book
    public String getName() {
        return name;
    }
    
    //Prints the 5 most recent rates of the book on the screen    
    public void printRecentRates() {      
        int i;
        System.out.println("\nRecent rates of the book");
        System.out.println("-------------------------");
        for(i=0; i<5; i++) { //Iterate through the list to print the recent rates
            if(rates[i] == null) {
                break;
            }
            else {
                System.out.println("\t"+ (i+1) + ". " + rates[i].getUser() + " rated " + rates[i].getRate() + " on " + rates[i].getTime());
            }
        }
        //If there are no recently given rates for the book        
        if(i == 0) {
            System.out.println("\nThere are no recent rate records!");
        }
    }
    
    //Calculates & returns the overall rate of the book
    public double getOverallRate() {
        Hashtable userTotal = market.getUserTotal(); //This Hashtable gives the total no of rates made by each of the user
        double sum_wr = 0;
        double sum_wk = 0;
        int r;
        double w;
        for(int i=0; i<index; i++) {
            w = 2.0 - 1.0/((Integer)userTotal.search(users[i].getName())).doubleValue(); //Weight of the ith user
            r = users[i].get_r(); //Sum of rates of the ith user for this book upto now
            sum_wr += w*r;
            sum_wk += w*users[i].get_k();
        }
        overallRate = sum_wr / sum_wk;
        return overallRate;
    }
    
    //Prints the overall aggregate rate of the book
    public void printOverallRate() {
        System.out.println("\nOverall Aggregate rate of the book : " + getOverallRate());
        System.out.println("----------------------------------");
    }
    
    //Displays the list of vendors in the descending order of their overall rates
    public void printTopRatedVendors() {
        vendors.heapSort(); //Use heap sort to efficiently sort the vendor list
        System.out.println("\nTop rated vendors of the book");
        System.out.println("-----------------------------");
        for(int i=vendors.getHeapSize()-1; i>=0; i--) {
            System.out.println((vendors.getHeapSize()-i) + ". " + vendors.getElement(i).getName() + " : " + vendors.getElement(i).getOverallRate());
        }
    } 
}

//Vendor class holds the information of each vendor in the online book market 
class Vendor {
    private String name; 
    private RatesEntry rates[]; //The recent rates are recorded in this 5 element rates array
    private double overallRate;
    private Hashtable userRegister; //Hashtable to hold the list of users to prevent duplicates in users[]
    private UserKey users[]; //Holds UserKey's with previous rating records of the particular book
    private int index; //To iterate through the users array
    private LinkedList books; //Linked list to hold the Book references of this vendor
    private Book_Market market; //Book_Market reference in which the Vendor is located in
    
    //Constructor for the Vendor which takes the name of the vendor and the book market
    Vendor(String name, Book_Market market) {
        this.name = name;
        this.market = market;
        rates = new RatesEntry[5]; //RatesEntry table is of size 5
        userRegister = new Hashtable(100); //I have given typical sizes for the hashtable & the array
        users = new UserKey[100];
        index = 0;
        books = new LinkedList();
        overallRate = 0;
    }
    
    //Entering a new rating record to the database        
    public void enterNewRate(Timestamp time, int rate, String user, Book book) {
        //This inserts the record to the "rates" array if it's recent        
        insertRate(new RatesEntry(time, rate, user));
        
        UserKey userKey;
        //UserKey to hold the previous rating records of the user
        //If the user rates the vendor for the first time then enter the user into the hashtable
        if(!userRegister.isExist(user)) { //Checks if the user is not in the Register
            userKey = new UserKey(user);
            userRegister.insert(userKey, user);
            users[index] = userKey; //Insert into the users array
            index++;
        }
        //If the user has rated before then simply load the previous UserKey for update        
        else {
            userKey = (UserKey)userRegister.search(user);
        }
        //Update the rating records in the hashtable accordingly - k value & rate        
        userKey.increment_k();
        userKey.enter_r(rate);
        
        //If the book is not in the linked list already then enter it into this Vendor's book list
        Object b = books.search(book.getName());
        if(b == null) {
            books.insert(book, book.getName());
        }
    }
    
    //This method inserts the rate into the recent "rates" array (with modified Insertion Sort)
    public void insertRate(RatesEntry r) {
        int i=0;
        //Checks whether a free space is available in the array
        while(i<5 && rates[i] != null) {
            i++;
        }
        if(i<5) { //If so insert the rate using insertion sort algorithm backward
            rates[i] = r;
            int j=i-1;
            while(j>=0 && rates[j].getTime().after(r.getTime())) {
                rates[j+1] = rates[j];
                j--;
            }
            rates[j+1] = r;
        }
        //If the array is full;
        //Then with the oldest rate to see whether the rate is recent
        //If so insert the rate using insertion sort algorithm forward
        else if(rates[0] != null && rates[0].getTime().before(r.getTime())) {
             rates[0] = r;
             int j=1;
             while(j<5 && rates[j].getTime().before(r.getTime())) {
                 rates[j-1] = rates[j];
                 j++;
             }
             rates[j-1] = r;
        }   
    }
    
    //Returns the name of the book
    public String getName() {
        return name;
    }
    
    //Prints the 5 most recent rates of the vendor on the screen    
    public void printRecentRates() {      
        int i;
        System.out.println("\nRecent rates of the vendor");
        System.out.println("--------------------------");
        for(i=0; i<5; i++) {
            if(rates[i] == null) {
                break;
            }
            else {
                System.out.println("\t" + (i+1) + ". " +rates[i].getUser() + " rated " + rates[i].getRate() + " on " + rates[i].getTime());
            }
        }
        if(i == 0) {
            //If there are no any recent rate for this vendor then;
            System.out.println("There are no recent rate records!");
        }
    }
    
    //Calculates & returns the overall rate of the vendor
    public double getOverallRate() {
        Hashtable userTotal = market.getUserTotal(); //This Hashtable gives the total no of rates made by each of the user
        double sum_wr = 0;
        double sum_wk = 0;
        int r;
        double w;
        for(int i=0; i<index; i++) {
            w = 2.0 - 1.0/((Integer)userTotal.search(users[i].getName())).doubleValue(); //Weight of the ith user
            r = users[i].get_r(); //Sum of rates of the ith user for this book upto now
            sum_wr += w*r;
            sum_wk += w*users[i].get_k();
        }
        overallRate = sum_wr / sum_wk;
        return overallRate;
    }
    
    //Prints the overall aggregate rate of the vendor
    public void printOverallRate() {
        System.out.println("\nOverall Aggregate rate of the vendor : " + getOverallRate());
        System.out.println("------------------------------------");
    }    
    
    //Displays the list of books available at this vendor with thier aggregative ratings
    public void printBookRates(Hashtable userTotal) {
        Object b = books.head; //Iterate through the linked list to print the books available in the list
        int i = 1;
        System.out.println("\nList of books with Overall Aggregate rates");
        System.out.println("------------------------------------------");
        Node node = (Node)b;
        while(node != null) {
            Book bb = (Book)node.key;
            System.out.println(i + ". " + bb.getName() + " : " + bb.getOverallRate());
            i++;
            node = node.next;
        }
    }
}

//UserKey class is used to keep the user name with other rating information of a book/vendor
class UserKey {
    private String name;
    private int r; //Sum of the ratings given by the user to the particular book/vendor
    private int k; //No of times the user has rated the particular book/vendor
    
    //Constructor for the UserKey
    UserKey(String name) {
        this.name = name;
        r = 0;
        k = 0;
    }
    //Returns the name of the user
    public String getName() {
        return name;
    }
    //Increment the no of times he has rated this book/vendor
    public void increment_k() {
        k++;
    }
    //Get the k value
    public int get_k() {
        return k;
    }
    //Add the rate to the sum of the rates upto now
    public void enter_r(int rate) {
        r += rate;
    }
    //Returns the sum of rates of the user for this book/vendor
    public int get_r() {
        return r;
    }
}

//Node(generic) to construct the linked list
class Node {
    Object key; //Key data of the node
    String name; //Name of the book/vendor/user
    Node next; //References to the next/ previous nodes
    Node prev;
    
    //Constructor for the Node
    Node(Object key, String name) {
        this.key = key;
        this.name = name; 
        next = null;
        prev = null;
    }
}

//Linked list made with Node
class LinkedList {
    Node head; //head Node of the linked list
        
    //Insert the object x whose associated with the name given
    public void insert(Object x, String name) {
        Node node = new Node(x, name); 
        node.next = head; //Link the next/ prev references appropriately
        if(head != null && head.next != null) {
            head.next.prev = node;
        }
        node.prev = null;
        head = node;
    }
    
    //Search for the required key with the associated name
    public Object search(String x) {
        Node node = head; 
        //Start from the head node and find until the required node is reached/list is over
        while(node != null && !(node.name.equals(x))) {
            node = node.next;
        }
        if(node != null) {
            return node.key;
        }
        else {
            return null;
        }
    }
    
    //Change the key data in the Node in the list given by the name
    public void changeData(String name, Object x) {
        Node node = head;
        //First search for the node & then change the value
        while(node != null && !(node.name.equals(name))) {
            node = node.next;
        }
        if(node != null) {
            node.key = x;
        }
    }
}

//Hashtable is used to access the books/vendors/users efficiently
class Hashtable {
    private LinkedList hash[]; //An array of linked lists
    private int size;
    private int index; //The slot index of the current data
    
    //Constructor for the Hashtable, the size given for the array 
    Hashtable(int size) {
        this.size = size;
        hash = new LinkedList[size];
        index = 0;
    }
    
    //Insert the object given into the linked list as the head node
    public void insert(Object x, String name) {
        index = calculateHash(name);
        if(hash[index] == null) {
            hash[index] = new LinkedList();
        }
        hash[index].insert(x, name);
    }
    
    //Calculates the hash function to get the slot index of a particular key
    private int calculateHash(String key) {
        index = 0;
        int sum = 0;
        //I have used the ASCII values with their positions to calculate the hash function
        for(int i=0; i<key.length(); i++) {
            sum += (int)key.charAt(i)*(i+1);
        }
        sum = sum % size;
        return sum;
    }
    
    //Checks whether the object with the given name is available in any of the linked lists in the hashtable
    public boolean isExist(String x) {
        index = calculateHash(x);
        if(hash[index] != null && hash[index].search(x) != null) {
            return true;
        }
        else {
            return false;
        }
    }
    
    //Search for the key of the object associated with the given name
    public Object search(String x) {
        index = calculateHash(x);
        return hash[index].search(x);
    }    
    
    //Change the data key of the object in the name given
    public void changeData(String x, Object y) {
        index = calculateHash(x);
        if(hash[index].search(x) != null) {
            hash[index].changeData(x, y);
        }
    }
}

//Max_Heap is not implemented generic, since I used it for Vendors
class Max_Heap {
    Vendor heap[]; //An array of vendors as the heap
    int size, heapSize, heapSize_before; //heapSize_before holds the size of the heap before being sorted
    
    //Constructor for the max_heap where the size is given
    Max_Heap(int size) {
        this.size = size;
        heap = new Vendor[size];
        heapSize = 0;
        heapSize_before = 0;
    }
    
    //Returns the size of the heap before being sorted
    public int getHeapSize() {
        return heapSize_before;
    }
    
    //Returns the Vendor at the index i in the heap
    public Vendor getElement(int i) {
        return heap[i];
    }
    
    //The object at index i is kept so the max heap property is maintained
    public void maxHeapify(int i) {
        int left = 2*i+1;
        int right = 2*i+2;
        int largest;

        if(left<heapSize && (heap[left] != null && heap[left].getOverallRate()>heap[i].getOverallRate())) {
            largest = left;
        }
        else {
            largest = i;
        }
        if(right<heapSize && (heap[left] != null && heap[right].getOverallRate()>heap[largest].getOverallRate())) {
            largest = right;
        }
        if(i != largest) {
            Vendor temp = heap[largest];
            heap[largest] = heap[i];
            heap[i] = temp;
            maxHeapify(largest);
        }
    }
    
    //The array of elements are arranged so that a max_heap is formed
    public void buildMaxHeap() {
        for(int i=((int)Math.floor(heapSize/2)); i>=0; i--) {
            maxHeapify(i); //Calls maxHeapify(..) method
        }
    }
    
    //Heap sort algorithm maintained in the max_heap of Vendor objects
    public void heapSort() {
        buildMaxHeap(); //Build the heap first
        for(int i=heapSize-1; i>-1; i--) {
            Vendor temp = heap[0];
            heap[0] = heap[i];
            heap[i] = temp;
            heapSize = heapSize - 1;
            maxHeapify(0);
        }
    }
    
    //Insert an element to the max_heap
    public void insert(Vendor v) {
        if (heapSize < size) {
            heapSize++;
            heapSize_before++;
            heap[heapSize - 1] = v;
            liftUp(heapSize - 1); //This method gets the element to upward so that max_heap property is maintained
        }
    }       
     
    //This method gets the element to upward so that max_heap property is maintained and the array is sorted
    private void liftUp(int index) {
        int parent;
        Vendor tmp;
        if (index != 0) {
            parent = (int)(Math.floor(index/2)); //Compare with the parent nodes to place the node at the correct position in the remaining max_heap
            if (heap[parent].getOverallRate() > heap[index].getOverallRate()) {
                 tmp = heap[parent];
                 heap[parent] = heap[index];
                 heap[index] = tmp;
                 liftUp(parent);
            }
        }
    }
}

//Book_Market object is used as a collection of books, vendors and users
public class Book_Market {
    
    private Hashtable books;
    private Hashtable vendors;
    private Hashtable users;
    private String file; //The namee of the input file
    private Scanner input; //The Scanner is used to read the user inputs/ inputs from the file as well
    
    //Constructor for the Book_Market
    Book_Market() {
        books = new Hashtable(500); //I have given typical sizes for the hashtables (according to the input file)
        vendors = new Hashtable(100);
        users = new Hashtable(100);
    }
    
    //Get the rating records from the input file
    public void inputFile(String fileName) throws IOException {
        file = fileName;
        
        input = new Scanner(new File(file));
        String line; //Line to be read by the scanner
        StringTokenizer tokens; //Tokens to split the line
        StringTokenizer timeTokens; //Tokens to split the date & time 
        
        //These are variables used temporily to get the inputs from the file
        Timestamp timeStamp; 
        String date_time;
        String date, time;
        String userName;
        String bookName;
        String vendorName;
        int bookRate, vendorRate;
        
        while(input.hasNextLine()) {
            line = input.nextLine(); //read line by line in the file
            
            //Now the line is tokenized
            tokens = new StringTokenizer(line);
            
            date_time = (String)tokens.nextElement();
            timeTokens = new StringTokenizer(date_time, "T");
            date = timeTokens.nextToken();
            time = timeTokens.nextToken();

            date_time = date + " " + time + ":00";
            timeStamp = Timestamp.valueOf(date_time);

            userName = tokens.nextToken();
            bookName = tokens.nextToken();
            vendorName = tokens.nextToken();
            
            vendorRate = Integer.parseInt(tokens.nextToken());
            bookRate = Integer.parseInt(tokens.nextToken());
            
            //These are references used temporily to get the inputs from the file
            Book b;
            Vendor v;
            int count;
            
            //Check the existance of the following books/vendors/users and if not create new
            //Then update the rating information
            if(books.isExist(bookName)) {
                b = (Book)books.search(bookName);
            }
            else {
                b = new Book(bookName, this);
                books.insert(b, bookName);
            }
            if(vendors.isExist(vendorName)) {
                v = (Vendor)vendors.search(vendorName);
            }
            else {
                v = new Vendor(vendorName, this);
                vendors.insert(v, vendorName);
            }
            if(!users.isExist(userName)) {
                users.insert(new Integer(0), userName);
            }
            
            //Count keeps the track of the current user's total no of rates for any of the book/vendor upto now
            //Everytime when an rating record is entered this count gets incremented accordingly
            count = ((Integer)users.search(userName)).intValue() + 1;
            users.changeData(userName, new Integer(count));

            //Enter the new rate records to the particular book/vendor
            b.enterNewRate(timeStamp, bookRate, userName, v);
            v.enterNewRate(timeStamp, vendorRate, userName, b);
        }
        //Colse the Scanner
        input.close();
            
    }
    
    //For simulation I have used the start(..) test harness method
    public void start(String file) {
        //Reading the input file
        try {
            inputFile(file);
        } catch(IOException e) {
            System.out.println("Error in reading the file " + file);
        }
     while(true) {   
        //Get the user input for the Main Menu of the online book market
        input = new Scanner(System.in);
        System.out.println("\n--****************Main Menu***************--");
        System.out.printf("\n\t1. Book Search\n\t2. Vendor Search\n\t3. Exit\n\nEnter your option : ");
        try {
            int option = input.nextInt();
        
        //Switch to handle the user inputs
        switch(option) {
            case 1: 
                System.out.println("\n------------Book Database------------");
                System.out.printf("Enter the book name : ");
                String book = input.next();
                if(!books.isExist(book)) {
                    System.out.println("Sorry! The book is not in the database.");
                }
                else {
                    printRecentRates_Book(book);
                    printOverallAggregrateRate_Book(book);
                    printTopRatedVendors_Book(book);
                }
                break;
            case 2:
                System.out.println("\n------------Vendor Database------------");
                System.out.printf("Enter the vendor name : ");
                String vendor = input.next();
                if(!vendors.isExist(vendor)) {
                    System.out.println("Sorry! The vendor is not in the database.");
                }
                else {
                    printRecentRates_Vendor(vendor);
                    printOverallAggregrateRate_Vendor(vendor);
                    printListOfBooksRates_Vendor(vendor);
                }
                break;
            case 3:
                System.exit(0);               
            default:
                System.out.printf("Wrong Input!\n");
        }       
        } catch(Exception e) {
            System.out.println("Invalid input! Try again..\n");
            continue;
        }
     }
    }
    
    //Returns the users hashtable in the Book_Market
    public Hashtable getUserTotal() {
        return users;
    }
    
    public void printRecentRates_Book(String name) {
       Book b = (Book)books.search(name);
       b.printRecentRates();
    }
    
    public void printOverallAggregrateRate_Book(String name) {
        Book b = (Book)books.search(name);
        b.printOverallRate();
    }
    
    public void printTopRatedVendors_Book(String name) {
        Book b = (Book)books.search(name);
            b.printTopRatedVendors();
    }
    
    public void printRecentRates_Vendor(String name) {
        Vendor v = (Vendor)vendors.search(name);
        v.printRecentRates();
    }
    
    public void printOverallAggregrateRate_Vendor(String name) {
        Vendor v = (Vendor)vendors.search(name);
        v.printOverallRate();
    }
    
    public void printListOfBooksRates_Vendor(String name) {
        Vendor v = (Vendor)vendors.search(name);
        v.printBookRates(users);
    }

    public static void main(String[] args) {
        Book_Market market = new Book_Market();
        System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*Online Book Market*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
        System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
        market.start("UserRating.txt");
    }
}