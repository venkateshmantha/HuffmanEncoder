import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class decoder {
	
	public static void main(String args[]){
		
		String encoded_file = args[0];
		String code_table_file = args[1];
		
		DecodeTree decode_tree = new DecodeTree();
		
		generateDecodeTree(code_table_file,decode_tree);
		generateDecoder(encoded_file,decode_tree);
		
	}

	private static void generateDecodeTree(String code_table_file,DecodeTree decode_tree) {
		
		try (BufferedReader br = new BufferedReader(new FileReader(code_table_file))) {

			String line;
			
			while ((line = br.readLine()) != null) {
				String[] pair = line.split(" ");
				int value = Integer.parseInt(pair[0]);
				String huffman_code = pair[1];
				DecodeTreeNode temp = decode_tree.root;
				for(int i=0;i<huffman_code.length()-1;i++){
					
					DecodeTreeNode latest_node = new DecodeTreeNode();
					if(huffman_code.charAt(i)=='0' && temp.left==null){
						 temp.left = latest_node;
						 temp=latest_node;
					}
					else if(huffman_code.charAt(i)=='0' && temp.left!=null)
						temp=temp.left;
					else if(huffman_code.charAt(i)=='1' && temp.right==null){
						temp.right=latest_node;
						temp=temp.right;
					}
					else if(huffman_code.charAt(i)=='1' && temp.right!=null)
						temp=temp.right;
					
				}
				
				DecodeTreeNode leaf = new DecodeTreeNode(value);
				if(huffman_code.charAt(huffman_code.length()-1)=='0')
					temp.left=leaf;
				else
					temp.right=leaf;
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void generateDecoder(String encoded_file, DecodeTree decode_tree) {
		
		String decode_file = "decoded.txt";
		ArrayList<Integer> decoded_values = new ArrayList<Integer>();
		
		try(InputStream inputStream = new FileInputStream(encoded_file)){
			
			DecodeTreeNode tmp = decode_tree.root;
			
			int byteRead;
			
			while ((byteRead = inputStream.read()) != -1) {
                
				String binary_str = Integer.toBinaryString(byteRead);
				if(binary_str.length()!=8){
					int extra_len = 8-binary_str.length();
					String padding_bits ="";
					while(extra_len>0){
						padding_bits+="0";
						extra_len--;
					}
					binary_str=padding_bits+binary_str;
				}
				for(int i=0;i<binary_str.length();i++){
					if(binary_str.charAt(i)=='0')
						tmp=tmp.left;
					else
						tmp=tmp.right;
					if(tmp.isLeaf){
						decoded_values.add(tmp.value);
						tmp=decode_tree.root;
					}
				}
            }
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		writeToFile(decoded_values,decode_file);
		
	}

	private static void writeToFile(ArrayList<Integer> decoded_values, String decode_file) {
		boolean isFirstLine = true;
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(decode_file,false))) {
			String content="";
			for(int val : decoded_values){
				if(isFirstLine){
					content = String.valueOf(val);
					isFirstLine = false;
				}
				else
					content= "\n" + String.valueOf(val);
				bw.write(content);
			}
			
		bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

class DecodeTreeNode {
	
	int value;
	DecodeTreeNode left;
	DecodeTreeNode right;
	boolean isLeaf;
	
	public DecodeTreeNode(){
		int value=-1;
		this.left=null;
		this.right=null;
		this.isLeaf=false;
		
	}
	
	public DecodeTreeNode(int value){
		this.value=value;
		this.left=null;
		this.right=null;
		this.isLeaf=true;
	}
	
}

class DecodeTree {
	
	DecodeTreeNode root;
	
	public DecodeTree(){
		this.root = new DecodeTreeNode();
	}
}
