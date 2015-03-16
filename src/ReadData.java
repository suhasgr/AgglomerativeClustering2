import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;


class tuple
{
	public int age;
	public int occupation;
	public HashMap<String, Integer> genre;
	public double rating;
	tuple llink;
	tuple rlink;
	int totalTuples;
	int male;
	int female;
};

public class ReadData 
{
	public static void agglomerative(ArrayList<tuple> tuples)
	{
		int size = tuples.size()-1;
		for(int i = 0; i < size; ++i)
		{
			//Getting the tuples with minimum distance and getting the distance in terms of Age and occupation
			
			int numberOfTuples = tuples.size();
			double minDistance = Double.MAX_VALUE;
			tuple left = null,right = null;
			
			for(int j = 0; j< numberOfTuples;++j)
			{
				tuple t1 = tuples.get(j);
				
				for(int k = j+1; k< numberOfTuples;++k)
				{
					tuple t2 = tuples.get(k);
					//double distance = (normalizedAgeHop(t1.age,t2.age) + ((t1.occupation == t2.occupation)?0:1))/2;

					HashMap<String, Integer> interGenre = new HashMap<String, Integer>();

					String[] S;
					if(t1.genre.keySet().toArray(new String[t1.genre.size()]).length < t2.genre.keySet().toArray(new String[t2.genre.size()]).length)
						S = t2.genre.keySet().toArray(new String[t2.genre.size()]);
					else
						S = t1.genre.keySet().toArray(new String[t1.genre.size()]);
					
					int total = 0,common = 0;
					for(int loc =0 ;loc< S.length; ++loc)
					{
						if(t1.genre.containsKey(S[loc]) && t2.genre.containsKey(S[loc]))
						{
							interGenre.put(S[loc],Math.min(t1.genre.get(S[loc]), t2.genre.get(S[loc])));
							common+=Math.min(t1.genre.get(S[loc]), t2.genre.get(S[loc]));
							total+=t1.genre.get(S[loc]) + t2.genre.get(S[loc]);
						}
						else if(!t1.genre.containsKey(S[loc]) && t2.genre.containsKey(S[loc]))
							total+=	t2.genre.get(S[loc]);
						else if(t1.genre.containsKey(S[loc]) && !t2.genre.containsKey(S[loc]))
							total+= t1.genre.get(S[loc]);
							
					}
					
					double distance = 1-Math.abs(t1.rating - t2.rating)/4;
					if(total!=0)
						distance+=(common* 1.0)/total;
					
					if(minDistance > distance)
					{
						minDistance = distance;
						left = t1;
						right = t2;
					}
				}
			}
			
			
			tuple clusterParent = new tuple();
			clusterParent.totalTuples = left.totalTuples + right.totalTuples;
			clusterParent.age = (left.age * left.totalTuples + right.totalTuples*right.age)/(left.totalTuples + right.totalTuples);
			
			clusterParent.occupation = left.totalTuples>right.totalTuples?left.occupation : right.occupation;

			clusterParent.llink = new tuple();
			clusterParent.rlink = new tuple();
			clusterParent.llink = left;
			clusterParent.rlink = right;
			
			String[] S;
			if(left.genre.keySet().toArray(new String[left.genre.size()]).length < right.genre.keySet().toArray(new String[right.genre.size()]).length)
				S = right.genre.keySet().toArray(new String[right.genre.size()]);
			else
				S = left.genre.keySet().toArray(new String[left.genre.size()]);
			
			clusterParent.genre = new HashMap<String,Integer>();
			for(int loc =0 ;loc< S.length; ++loc)
			{
				if(left.genre.containsKey(S[loc]) && right.genre.containsKey(S[loc]))
					clusterParent.genre.put(S[loc],Math.min(left.genre.get(S[loc]), right.genre.get(S[loc])));
			}

			clusterParent.male = left.male + right.male;
			clusterParent.female = left.female + right.female;
			
			clusterParent.rating = (left.rating * left.totalTuples + right.rating * right.totalTuples)/(left.totalTuples + right.totalTuples);
			
			tuples.remove(left);
			tuples.remove(right);
			
			tuples.add(clusterParent);
//			System.out.println("Iteration = "+i+"  size of tuples = "+tuples.size());
		}
		
		System.out.println("Done");
	}
	
	public static void SimilarityGenresandRating(ArrayList<tuple> tuples)
	{
		//Travelling 3 levels for checking the similarity
		
		Queue q = new LinkedList<tuple>();
		q.add(tuples.get(0));
		
		int level = 1;
		while(!q.isEmpty())
		{
			tuple t= (tuple) q.remove();
			System.out.println("*****************At Level = "+Math.ceil(Math.log(level)));
			System.out.println("Male ratio ="+ (t.male*1.0)/(t.male +t.female)+ " Female ratio = "+ (t.female * 1.0)/(t.male+t.female));
			System.out.println("Common Age group is = "+t.age);
			System.out.println("Genres Present in this cluster = ");
			Iterator it = t.genre.entrySet().iterator();
			
			while(it.hasNext())
			{
				Map.Entry pair = (Map.Entry)it.next();
				System.out.println(pair.getKey());
			}
			
			System.out.println("Average rating of the users in this age group id = "+t.rating);
			System.out.println("Common Profession is = "+t.occupation);
			if(level<8)
			{
				++level;
				if(t.llink != null)
					q.add(t.llink);
				if(t.rlink != null)
					q.add(t.rlink);
			}
		}
	}
	
	public static void main(String[] args)
	{
		String sql;
	    sql = "SELECT m.genre,r.rating,u.age,u.gender,u.occupation FROM users1 u, movies1 m, ratings1 r where " +
	      		"u.userid = r.userid and m.movieid = r.movieid";
	    ArrayList<tuple> tuples = new ArrayList<tuple>();
	    
	    MySQLConnection mysqlConnection = new MySQLConnection("suhas_db", "root", "root");
	    boolean status = mysqlConnection.getData(sql, tuples);
		
		if(status == false)
		{
			System.out.println("Wrong in data fetch");
			System.exit(0);
		}
		else
		{
			System.out.println("Total Tuple Strength = "+tuples.size());
		}
		
		agglomerative(tuples);
		
		mysqlConnection.stop();
		
		SimilarityGenresandRating(tuples);
		
	}

}

