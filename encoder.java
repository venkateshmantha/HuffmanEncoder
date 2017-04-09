import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public class encoder {
	
	private static String FILENAME = "";
	
	static HashMap<Integer,String> code_table = new HashMap<Integer,String>();
	
	static ArrayList<Integer> listForBinary = new ArrayList<Integer>();

	public static void main(String[] args) {
		
		FILENAME = args[0];
		
		/** Building frequency map **/
		
		HashMap<Integer,Integer> freq_map = new HashMap<Integer,Integer>();
		
		int line_count=0;

		try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {

			String line;
			
			while ((line = br.readLine()) != null) {
				line_count++;
				listForBinary.add(Integer.parseInt(line));
				if(freq_map.get(Integer.parseInt(line))==null)
					freq_map.put(Integer.parseInt(line), 1);
				else
					freq_map.put(Integer.parseInt(line), freq_map.get(Integer.parseInt(line))+1);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/** Performance analysis **/
		
		/*long start_time_binary = System.currentTimeMillis();
		for(int i=0;i<10;i++){
			build_huffman_tree_using_binary_heap(freq_map,line_count);
		}
		long elapsed_time_binary = (System.currentTimeMillis()-start_time_binary)/10;
		
		System.out.println("Elapsed time using binary heap is " + elapsed_time_binary + " ms");
		
		
		long start_time_dway = System.currentTimeMillis();
		for(int i=0;i<10;i++){
			build_huffman_tree_using_dway_heap(freq_map,line_count);
		}
		long elapsed_time_dway = (System.currentTimeMillis()-start_time_dway)/10;
		
		System.out.println("Elapsed time using 4 way heap is " + elapsed_time_dway + " ms");
		
		long start_time_pair = System.currentTimeMillis();
		for(int i=0;i<10;i++){
			build_huffman_tree_using_pairing_heap(freq_map);
		}
		long elapsed_time_pair = (System.currentTimeMillis()-start_time_pair)/10;
		
		System.out.println("Elapsed time using pairing heap is " + elapsed_time_pair + " ms");*/
		
		/** Build huffman tree using 4-way cache optimized heap **/
		
		build_huffman_tree_using_dway_heap(freq_map,line_count);
		

	}
	
	/** Huffman tree generation **/

	private static void build_huffman_tree_using_binary_heap(HashMap<Integer, Integer> freq_map, int line_count) {
		
		Iterator it = freq_map.keySet().iterator();
		BinaryHeap heap = new BinaryHeap(line_count);
		
		while(it.hasNext()){
			int val = (int) it.next();
			int freq = freq_map.get(val);
			BinaryNode bn = new BinaryNode(val,freq);
			heap.insert(bn);	
		}
		
		while(heap.getHeapSize()>1){
			
			BinaryNode min1 = heap.deleteMin();
			BinaryNode min2 = heap.deleteMin();
			int val = -1;
			int freq = min1.getFreq()+min2.getFreq();
			BinaryNode bn_new = new BinaryNode(val,freq,min1,min2);
			heap.insert(bn_new);
			
		}
		
		//heap.printHeap();
	}
	
	private static void build_huffman_tree_using_pairing_heap(HashMap<Integer, Integer> freq_map) {
		
		Iterator it = freq_map.keySet().iterator();
		PairHeap pair_heap = new PairHeap();
		
		while(it.hasNext()){
			int val = (int) it.next();
			int freq = freq_map.get(val);
			PairNode pn = new PairNode(val,freq,null,null);
			pair_heap.insert(pn);	
		}
		
		while(pair_heap.root.leftChild!=null){
			PairNode min1 = pair_heap.deleteMin();
			PairNode min2 = pair_heap.deleteMin();
			int val = -1;
			int freq = min1.freq + min2.freq;
			PairNode new_pn = new PairNode(val,freq,min1,min2);
			pair_heap.insert(new_pn);
		}
		
	}
	
	private static void build_huffman_tree_using_dway_heap(HashMap<Integer, Integer> freq_map, int line_count) {
		
		Iterator it = freq_map.keySet().iterator();
		dwayHeap dway_heap = new dwayHeap(line_count);
		
		while(it.hasNext()){
			int val = (int) it.next();
			int freq = freq_map.get(val);
			dwayNode dn = new dwayNode(val,freq);
			dway_heap.insert(dn);	
		}
		
		while(dway_heap.getHeapSize()>1){
			
			dwayNode min1 = dway_heap.deleteMin();
			dwayNode min2 = dway_heap.deleteMin();
			int val = -1;
			int freq = min1.getFreq() + min2.getFreq();
			dwayNode dn_new = new dwayNode(val,freq,min1,min2);
			dway_heap.insert(dn_new);
		}
		
		dwayNode root = dway_heap.heap[3];
		
		/** Build code table **/
		buildCodes(root,"");
		
		/** Write code_table.txt **/
		writeToFile(code_table);
		
		/** Write encoded.bin **/
		writeToBinaryFile(listForBinary,code_table);
	}

	private static void buildCodes(dwayNode n, String path) {
		
		if (n.getLeft() != null)
			buildCodes(n.getLeft(), path+"0");
		if (n.getRight() != null)
			   buildCodes(n.getRight(), path+"1");
		if (n.getLeft() == null && n.getRight() == null) { 
			   code_table.put(n.getVal(), path);
		}
		
	}
	
	private static void writeToFile(HashMap<Integer, String> code_table) {
		
		String filename = "code_table.txt";
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename,false))) {

			Iterator it = code_table.entrySet().iterator();
			boolean firstLine = true;
			while(it.hasNext()){
				Entry entry = (Entry) it.next();
				String content ="";
				if(firstLine){
					content = Integer.toString((int) entry.getKey()) + " " + (String) entry.getValue();
					firstLine = false;
				}
				else
					content = "\n" + Integer.toString((int) entry.getKey()) + " " + (String) entry.getValue();
				bw.write(content);
			}
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void writeToBinaryFile(ArrayList<Integer> listForBinary, HashMap<Integer, String> code_table) {
		
		String filename = "encoded.bin";
		
		try (FileOutputStream fos  = new FileOutputStream(new File(filename),false)) {
			
			String code_buffer ="";
			Iterator it = listForBinary.iterator();
			while(it.hasNext()){
				String code = code_table.get(it.next());
				String current_code = code_buffer+code;
				
				int len = current_code.length();
				int mul = len-len%8;
				
				if(len<8)
					code_buffer = current_code;
			
				else{
					int start_index=0;	
					int end_index =8;
					while(end_index <= mul){
						byte binary = (byte)Integer.parseInt(current_code.substring(start_index, end_index),2);
						start_index=end_index;
						end_index+=8;
						fos.write(binary);
					}
					
					code_buffer = current_code.substring(mul, len);
				}
			}
			
			fos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}

class BinaryNode {
	
	private int val=-1;
	private int freq=-1;
	private BinaryNode left;
	private BinaryNode right;
	
	public int getVal() {
		return val;
	}
	public void setVal(int val) {
		this.val = val;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public BinaryNode getLeft() {
		return left;
	}
	public void setLeft(BinaryNode left) {
		this.left = left;
	}
	public BinaryNode getRight() {
		return right;
	}
	public void setRight(BinaryNode right) {
		this.right = right;
	}
	
	public BinaryNode(int val, int freq){
		this.val = val;
		this.freq = freq;
		this.left = null;
		this.right = null;
	}
	
	public BinaryNode(int val, int freq, BinaryNode left, BinaryNode right){
		this.val = val;
		this.freq = freq;
		this.left = left;
		this.right = right;
	}

}

class BinaryHeap {
	
	// Number of child nodes
	private static final int d = 2;
	
	private int heap_size;
	private BinaryNode[] heap;
	
	public BinaryHeap(int capacity){
		heap_size=0;
		heap = new BinaryNode[capacity];
	}
	
	/** Function to check if heap is empty **/
	
    public boolean isEmpty( )
    {
        return heap_size == 0;
    }
	
    /** Check if heap is full **/
    
    public boolean isFull( )
    {
        return heap_size == heap.length;
    }
    
    /** Return indices of parent and children **/
    
	private int parent(int i) 
    {
        return i/d;
    }
	
	private int left(int i) 
    {
        return d*i;
    }
	
	private int right(int i) 
    {
        return d*i+1;
    }
	
	/** Insert new node **/
	
	public void insert(BinaryNode node)
    {

		if (isFull( ))
            throw new NoSuchElementException("Heap is full");
		heap_size++;
        int i = heap_size;
        while(i > 1 && heap[parent(i)].getFreq() > node.getFreq()) {
                heap[i] = heap[parent(i)];
                i = parent(i);
        }
        heap[i] = node;
    }
	
	public BinaryNode findMin( )
    {          
		if (isEmpty())
            throw new NoSuchElementException("Heap is empty");
        return heap[1];
    }
	
	/** Delete Min **/
	
	public BinaryNode deleteMin()
    {
		if (isEmpty())
            throw new NoSuchElementException("Heap is empty");
        BinaryNode min_node = heap[1];
        heap[1] = heap[heap_size];
        heap_size--;
        heapify(1);
        return min_node;
    }
	
	/** Percolate Down **/
	 
	public void heapify(int i) {
        int l = left(i);
        int r = right(i);
        int smallest;
        if(r <= heap_size) {
                if(heap[l].getFreq() < heap[r].getFreq())
                        smallest = l;
                else
                        smallest = r;
                if(heap[i].getFreq() > heap[smallest].getFreq()) {
                        swap(i, smallest);
                        heapify(smallest);
                }
        }
        else if(l == heap_size && heap[i].getFreq() > heap[l].getFreq()) {
                swap(i, l);
        }               
	}
	
	private void swap(int i, int l) {
        BinaryNode tmp = heap[i];
        heap[i] = heap[l];
        heap[l] = tmp;
	}
	
	 
    public void printHeap()
    {
        System.out.print("Heap = ");
        for (int i=1; i<=heap_size; i++)
            System.out.print(heap[i].getFreq() +" ");
        System.out.println();
    }
    
    public int getHeapSize(){
    	return heap_size;
    }

}


class dwayHeap {
	
	// Number of children
	private static final int d = 4;
	
	private int heap_size;
	dwayNode[] heap;
	
	public dwayHeap(int capacity){
		heap_size=0;
		heap = new dwayNode[capacity];
	}
	
	/** Function to check if heap is empty **/
	
    public boolean isEmpty( )
    {
        return heap_size == 0;
    }
	
    /** Check if heap is full **/
    
    public boolean isFull( )
    {
        return heap_size == heap.length;
    }
    
    /** Return indices of parent and children **/
    
	private int parent(int i) 
    {
        return i/d + 2;
    }
	
	private int kthChild(int i, int k) 
    {
        return d*(i-2) + k;
    }
	
	/** Insert new node **/
	
	public void insert(dwayNode node)
    {

		if (isFull( ))
            throw new NoSuchElementException("Heap is full");
		heap_size++;
        int i = heap_size+2;
        while(i > 3 && heap[parent(i)].getFreq() > node.getFreq()) {
                heap[i] = heap[parent(i)];
                i = parent(i);
        }
        heap[i] = node;
    }
	
	public dwayNode findMin( )
    {          
		if (isEmpty())
            throw new NoSuchElementException("Heap is empty");
        return heap[3];
    }
	
	/** Delete min **/
	
	public dwayNode deleteMin()
    {
		if (isEmpty())
            throw new NoSuchElementException("Heap is empty");
        dwayNode min_node = heap[3];
        heap[3] = heap[heap_size+2];
        heap_size--;
        heapify(3);
        return min_node;
    }
	
	/** Percolate Down **/
	 
	private void heapify(int ind)
    {
        int child;
        int tmp = heap[ind].getFreq();
        dwayNode tmp_node = heap[ind];
        while (kthChild(ind, 0) <= heap_size+2)
        {
            child = minChild(ind);
            if (heap[child].getFreq() < tmp)
                heap[ind] = heap[child];
            else
                break;
            ind = child;
        }
        heap[ind] = tmp_node;
    }
 
    /** Function to get smallest child **/
	
    private int minChild(int ind) 
    {
        int bestChild = kthChild(ind, 0);
        int k = 1;
        int pos = kthChild(ind, k);
        while ((k < d) && (pos <= heap_size+2)) 
        {
            if (heap[pos].getFreq() < heap[bestChild].getFreq()) 
                bestChild = pos;
            pos = kthChild(ind, ++k);
        }    
        return bestChild;
    }
	
	private void swap(int i, int l) {
        dwayNode tmp = heap[i];
        heap[i] = heap[l];
        heap[l] = tmp;
	}
	
	 
    public void printHeap()
    {
        System.out.print("Heap = ");
        for (int i=3; i<=heap_size+2; i++)
            System.out.print(heap[i].getFreq() +" ");
        System.out.println();
    }
    
    public int getHeapSize(){
    	return heap_size;
    }
		

}

class dwayNode {
	
	private int val=-1;
	private int freq=-1;
	private dwayNode left;
	private dwayNode right;
	
	public int getVal() {
		return val;
	}
	public void setVal(int val) {
		this.val = val;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public dwayNode getLeft() {
		return left;
	}
	public void setLeft(dwayNode left) {
		this.left = left;
	}
	public dwayNode getRight() {
		return right;
	}
	public void setRight(dwayNode right) {
		this.right = right;
	}
	public dwayNode(int val, int freq){
		this.val = val;
		this.freq = freq;
	}
	
	public dwayNode(int val, int freq, dwayNode left, dwayNode right){
		this.val = val;
		this.freq = freq;
		this.left = left;
		this.right = right;
	}

}

class PairHeap {
	
	PairNode root; 
	
	/** Array to store pointers to children subtrees **/
    private PairNode [ ] treeArray = new PairNode[1000];
    
    public PairHeap( )
    {
        root = null;
    }
    
    public boolean isEmpty() 
    {
        return root == null;
    }
    
    public void insert(PairNode newNode)
    {
        if (root == null)
            root = newNode;
        else
            root = compareAndLink(root, newNode);
    }
    
    /** Function compareAndLink **/
    
    private PairNode compareAndLink(PairNode first, PairNode second)
    {
        if (second == null)
            return first;
 
        if (second.freq < first.freq)
        {
            /** Attach first as leftmost child of second **/
            second.prevSibling = first.prevSibling;
            first.prevSibling = second;
            first.nextSibling = second.leftChild;
            if (first.nextSibling != null)
                first.nextSibling.prevSibling = first;
            second.leftChild = first;
            return second;
        }
        else
        {
            /** Attach second as leftmost child of first **/
            second.prevSibling = first;
            first.nextSibling = second.nextSibling;
            if (first.nextSibling != null)
                first.nextSibling.prevSibling = first;
            second.nextSibling = first.leftChild;
            if (second.nextSibling != null)
                second.nextSibling.prevSibling = second;
            first.leftChild = second;
            return first;
        }
    }
    
    /** Function to combine siblings **/
    
    private PairNode combineSiblings(PairNode firstSibling)
    {
        if( firstSibling.nextSibling == null )
            return firstSibling;
        
        /** Store the subtrees in an array **/
        
        int numSiblings = 0;
        for ( ; firstSibling != null; numSiblings++)
        {
            treeArray = doubleIfFull( treeArray, numSiblings );
            treeArray[ numSiblings ] = firstSibling;
            /** break links **/
            firstSibling.prevSibling.nextSibling = null;  
            firstSibling = firstSibling.nextSibling;
        }
        
        treeArray = doubleIfFull( treeArray, numSiblings );
        treeArray[ numSiblings ] = null;
        
        /** Combine subtrees two at a time, going left to right **/
        
        int i = 0;
        for ( ; i + 1 < numSiblings; i += 2)
            treeArray[ i ] = compareAndLink(treeArray[i], treeArray[i + 1]);
        
        /** j has the result of last compareAndLink **/
        int j = i - 2;
        
        /** If an odd number of trees, get the last one **/
        if (j == numSiblings - 3)
            treeArray[ j ] = compareAndLink( treeArray[ j ], treeArray[ j + 2 ] );
        
        /** Now go right to left, merging last tree with 
         	next to last. The result becomes the new last **/
        
        for ( ; j >= 2; j -= 2)
            treeArray[j - 2] = compareAndLink(treeArray[j-2], treeArray[j]);
        return treeArray[0];
    }
    
    private PairNode[] doubleIfFull(PairNode [ ] array, int index)
    {
        if (index == array.length)
        {
            PairNode [ ] oldArray = array;
            array = new PairNode[index * 2];
            for( int i = 0; i < index; i++ )
                array[i] = oldArray[i];
        }
        return array;
    }
    
    /** Delete min element **/
    
    public PairNode deleteMin( )
    {
        if (isEmpty( ))
            return null;
        PairNode min_node = root;
        if (root.leftChild == null)
            root = null;
        else
            root = combineSiblings( root.leftChild );
        return min_node;
    }

}

class PairNode {
	
	int val;
	int freq;
    PairNode leftChild;
    PairNode nextSibling;
    PairNode prevSibling;
    PairNode left;
    PairNode right;
 
    public PairNode(int val, int freq, PairNode left, PairNode right)
    {
        this.val = val;
        this.freq = freq;
        leftChild = null;
        nextSibling = null;
        prevSibling = null;
        this.left = left;
        this.right = right;
    }

}


