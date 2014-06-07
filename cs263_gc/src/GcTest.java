import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class GcTest 
{
	public static void main(String []args)
	{
	
		A a = new A();
		GcSimulator.create("a",a);
		System.out.println("create a:" + a );
		
		A b = new A();
		GcSimulator.create("b",b);
		System.out.println("create b:" + b );
	

		
		// heap 
	    C c = new C();
	    GcSimulator.create("c",c);
	    System.out.println("create c:" + c );
	    c.childA = b;
	    GcSimulator.write("c", "b", "childA", c, b);		
	    
		b = a;				
		GcSimulator.assign("b", "a", a);
		
		c.childA = b;
		GcSimulator.write("c", "a", "childA", c, a);
	    
		GcSimulator.gc();
		
		
	}
}
class A
{
	public int fieldA;
	public void methodA()
	{
		System.out.println("class A method");
	}
		
}
class B extends A
{
	public int fieldB;
	public void methodA()
	{
		System.out.println("class B method overrides A");
	}
	public void methodB()
	{
		System.out.println("class B method");
	}
}

class C
{
	int fieldC;
	public A childA;
	public B childB;
}
class GcSimulator
{
	public static HashMap<String,Object> stackRef = new HashMap<String,Object>();
	public static HashMap<Object,Map<String,Object>> heapRef = new HashMap<Object,Map<String,Object>>();
	public static ArrayList<Object> objectSet = new ArrayList<Object>();
	public static ArrayList<Object> objectLive= new ArrayList<Object>();
	
	
	public static void create(String var, Object o)
	{
		stackRef.put(var, o);	
		objectSet.add(o);		
	}
	
	public static void assign(String c, String b, Object o  )  // B c = b;
	{
		stackRef.put(c,o);		
	}

	
//	P p = a.m;
 //   GCSimulator.readObject(¡°p¡±, ¡°a¡±,  p);
	
	public static void read(String p, String a, Object o)
	{
		stackRef.put(p,o);
		
	}
	//Object write:  a.f = b;
	public static void write(String aName, String bName,String fName,Object a,Object b)
	{
		heapRef.put(a, null);
		Map<String,Object> map = (Map<String,Object>)heapRef.get(a);
		
		map = new HashMap();
		map.put(fName, b);
		heapRef.put(a, map);

	//	((Map<String,Object>)heapRef.get(a)).put(fName, b);		// <a,<fName,b>>;
	}
	public static void dfs(Object o)  // <a,<fName,b>>;  o=a,  map=<fname,b>
	{
		objectLive.add(o);	
		Map<String,Object> map = (Map<String,Object>)heapRef.get(o);
		if (map == null)
			return;
		Iterator iter = map.values().iterator();
		while(iter.hasNext())
		{
			Object value = (Object)iter.next();
			dfs(value);
	
		}
			
		
	}
	public static void gc()
	{
	//	Map<String,Object> heapRef.values() = new Map<String,Object>;
		
		// stackRef traverse
		Object value = null;
		Collection c = stackRef.values();
		Iterator iter= c.iterator();
		while (iter.hasNext())
		{
		    value = (Object)iter.next();
		    objectLive.add(value);
		}
		
		// heapRef traverse
		iter = heapRef.keySet().iterator();
		while(iter.hasNext())
		{
			Object key = iter.next();
			if (objectLive.contains(key))  // live heap obj
			{
				dfs(key);
			}
		}
		
		
		// check
		iter = objectSet.iterator();
		while(iter.hasNext())
		{
			value = (Object)iter.next();
			
			if (objectLive.contains(value) == false)
			{
				System.out.println("Garbage: " + value);
			}
			else
			{
				System.out.println("Live: " + value);
			}
	
		}
		
	}
}
