
public class ArrayList<T> {

	private int capacity;
	private T[] array;
	private int size;
	
	
	public ArrayList()
	{
		this.size = 0;
		array = createArray(this.capacity);
		this.capacity = 1;
	}
	
	
	@SuppressWarnings("unchecked")
	public T[] createArray(int size)
	{
		return (T[]) new Object[size];
	}
	
	public int size()
	{
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}

	
	
	public T get(int index)
	{
		if (index < 0 || index >= size)
		{
			System.out.print("Index is out of range");
		}
		return array[index];
	}

	public void set(int index, T element)
	{
		if (index < 0 || index > size)
		{
			System.out.print("Index is out of range");
		}
		
		array[index] = element;
		
	}
	
	public void add (int index, T element)
	{
		//Check if the index is within range
		if (index < 0 || index > size+1)
		{
			System.out.print("Index is out of range");
		}
		//Check if the array is full
		if (size == array.length)
		{
			System.out.println("Array is full!");
			expandArray();
		}
		
		//Shift elements to the right to make space for the new element
		for (int i = size - 1; i >= index; i--)
		{
			array[i + index] = array[i];
		}
		//Add element
		array[index] = element;
		size++;
	}
	
	private void expandArray()
	{
		int newCap;
		
		newCap = capacity * 2;
		
		T[] newArray = createArray(newCap);
		for(int i = 0; i < size; i++)
		{
			newArray[i] = array[i];
		}
		
		array = newArray;
		capacity = newCap;
	}
	
	public T remove(int index)
	{
		if (index < 0 || index > size)
		{
			System.out.print("Index is out of range");
		}
		
		T removed = array[index];
		
		//Shift elements to the left
		for (int i = index; i < size - 1; i++)
		{
			array[i] = array[i + 1];
		}
		
		size--;
		return removed;
	}
	
	public String toString()
	{
		String str = "[";
		for (int i = 0; i < size - 1; i++)
		{
			str += array[i].toString() + ",";
		}
		if (size > 0)
		{
			str += array[size - 1];
		}
		str += "]";
		return str;
	}
	
}
