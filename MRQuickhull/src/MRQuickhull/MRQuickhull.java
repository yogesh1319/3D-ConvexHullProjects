
package MRQuickhull;
import java.lang.Math;
import java.util.Collections;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapred.JobConf;
import java.util.ArrayList;
//import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.conf.Configuration;
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.TextInputFormat;
//import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit; 
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.mapred.JobClient;
import java.io.*;
import java.util.*;
import java.net.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapred.Counters;

public class MRQuickhull {

    
 static Vertex[] maxVtxs = new Vertex[3];
 static Vertex[] minVtxs = new Vertex[3];
 protected static ArrayList<Face> faces = new ArrayList<Face>();
 
 private static Face[] discardedFaces = new Face[3];
 
 protected static Vector horizon = new Vector(16);

 private static FaceList newFaces = new FaceList();
 
 private static final int NONCONVEX_WRT_LARGER_FACE = 1;
 private static final int NONCONVEX = 2;
 
 
 public static final double AUTOMATIC_TOLERANCE = -1;
 // estimated size of the point set
 protected static double charLength;
        
 protected static double explicitTolerance = AUTOMATIC_TOLERANCE;
 protected static double tolerance;
        
 static private final double DOUBLE_PREC = 2.2204460492503131e-16;
    
        
        
        
        
 public static class MaxMin1Mapper 
       extends Mapper<Object, Text, Text, Text>{
    
    private Text TKey = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
        ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
        
        String line = value.toString();
        
        if (line.length()>1){
        String []linesArr = line.split("\n");
        
        
        for (int i=0; i<linesArr.length;i++){
        
            String[] S=linesArr[i].split(" ");
          if (S.length>=3){
            Vertex TVertex=new Vertex();
            
            TVertex.pnt.x=Double.valueOf(S[0]);
            TVertex.pnt.y=Double.valueOf(S[1]);
            TVertex.pnt.z=Double.valueOf(S[2]);
            
            PointsArr.add(TVertex);
          }    
        }
        
        Vertex[] maxVtxs = new Vertex[3];
        Vertex[] minVtxs = new Vertex[3];
        
        Vector3d max = new Vector3d();
	Vector3d min = new Vector3d();

	   for (int i=0; i<3; i++)
	    { maxVtxs[i] = minVtxs[i] = PointsArr.get(0); 
	    }
	   
           
           max.set (PointsArr.get(0).pnt);
	   min.set (PointsArr.get(0).pnt);

	   for (int i=1; i<PointsArr.size(); i++)
	    
           { Point3d pnt = PointsArr.get(i).pnt;
	      if (pnt.x > max.x)
	       { max.x = pnt.x;
		 maxVtxs[0] = PointsArr.get(i);
	       }
	      else if (pnt.x < min.x)
	       { min.x = pnt.x;
		 minVtxs[0] = PointsArr.get(i);
	       }
	      if (pnt.y > max.y)
	       { max.y = pnt.y;
		 maxVtxs[1] = PointsArr.get(i);
	       }
	      else if (pnt.y < min.y)
	       { min.y = pnt.y;
		 minVtxs[1] = PointsArr.get(i);
	       }
	      if (pnt.z > max.z)
	       { max.z = pnt.z;
		 maxVtxs[2] = PointsArr.get(i);
	       }
	      else if (pnt.z < min.z)
	       { min.z = pnt.z;
		 minVtxs[2] = PointsArr.get(i);
	       }
	    }

        
           
       TKey.set("Reducer1");    
       for (int i=0; i<3;i++){
       
       String S=maxVtxs[i].pnt.x+" "+maxVtxs[i].pnt.y+" "+maxVtxs[i].pnt.z;
       Text T=new Text(S);
       
       context.write(TKey,T);
       
       S=minVtxs[i].pnt.x+" "+minVtxs[i].pnt.y+" "+minVtxs[i].pnt.z;
       T.set(S);
       
       context.write(TKey,T);
       
       
       
       }  
        
    }
     
       
      
    }
 }
      
 public static class MaxMin1Reducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   

    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      
        Configuration conf = context.getConfiguration();
        
        String MaxMinS=conf.get("MaxMinS");
        String MaxS=conf.get("MaxS");
        String MinS=conf.get("MinS");
        
         NullWritable N=null;
         ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
         
            for (Text val : values) {
                 String line = val.toString();
                 String []linesArr = line.split(" ");
                 Vertex TVertex=new Vertex();
                 TVertex.pnt.x=Double.valueOf(linesArr[0]);
                 TVertex.pnt.y=Double.valueOf(linesArr[1]);
                 TVertex.pnt.z=Double.valueOf(linesArr[2]);
            
                 PointsArr.add(TVertex);
          
            }
            
        Vertex[] maxVtxs = new Vertex[3];
        Vertex[] minVtxs = new Vertex[3];
        
        Vector3d max = new Vector3d();
	Vector3d min = new Vector3d();

	   for (int i=0; i<3; i++)
	    { maxVtxs[i] = minVtxs[i] = PointsArr.get(0); 
	    
            }
	   
           
           max.set (PointsArr.get(0).pnt);
	   min.set (PointsArr.get(0).pnt);

	   for (int i=1; i<PointsArr.size(); i++)
	    
           { Point3d pnt = PointsArr.get(i).pnt;
	      if (pnt.x > max.x)
	       { max.x = pnt.x;
		 maxVtxs[0] = PointsArr.get(i);
	       }
	      else if (pnt.x < min.x)
	       { min.x = pnt.x;
		 minVtxs[0] = PointsArr.get(i);
	       }
	      if (pnt.y > max.y)
	       { max.y = pnt.y;
		 maxVtxs[1] = PointsArr.get(i);
	       }
	      else if (pnt.y < min.y)
	       { min.y = pnt.y;
		 minVtxs[1] = PointsArr.get(i);
	       }
	      if (pnt.z > max.z)
	       { max.z = pnt.z;
		 maxVtxs[2] = PointsArr.get(i);
	       }
	      else if (pnt.z < min.z)
	       { min.z = pnt.z;
		 minVtxs[2] = PointsArr.get(i);
	       }
	    }

        
           
           
       for (int i=0; i<3;i++){
       
       String S=maxVtxs[i].pnt.x+" "+maxVtxs[i].pnt.y+" "+maxVtxs[i].pnt.z;
       Text T=new Text(S);
       MaxMinS=MaxMinS+S+"=";
      // context.write(N,T);
       
       S=minVtxs[i].pnt.x+" "+minVtxs[i].pnt.y+" "+minVtxs[i].pnt.z;
       T.set(S);
       MaxMinS=MaxMinS+S+"=";
       //context.write(N,T);
       
       
       
       }  
     
   context.write(N,new Text("MaxMinS"+"_"+MaxMinS));
   MaxS=max.x+" "+max.y+" "+max.z;
   MinS=min.x+" "+min.y+" "+min.z;
   context.write(N,new Text("MaxS"+"_"+MaxS));
   context.write(N,new Text("MinS"+"_"+MinS));
   
   
    }
  }
         
 /*****************************************************************************/
 
 public static class MaxMin2Mapper 
       extends Mapper<Object, Text, Text, Text>{
    
    private Text TKey = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
           
           Vector3d u01 = new Vector3d();
	   Vector3d diff02 = new Vector3d();
	   Vector3d nrml = new Vector3d();
	   Vector3d xprod = new Vector3d();
           Vertex vtx0=new Vertex();
           Vertex vtx1=new Vertex();
	   Vertex vtx2=new Vertex();
           double maxSqr = 0;
        
        
           Configuration conf = context.getConfiguration();
        
           String[] u01S=conf.get("u01S").split(" ");
           String[] vtx0S=conf.get("vtx0S").split(" ");
           String[] vtx1S=conf.get("vtx1S").split(" ");
           //String xprodS=xprod.toString();
           //String nrmlS=nrml.toString();
           
           u01.x=Double.valueOf(u01S[0]);
           u01.y=Double.valueOf(u01S[1]);
           u01.z=Double.valueOf(u01S[2]);
           
           vtx0.pnt.x=Double.valueOf(vtx0S[0]);
           vtx0.pnt.y=Double.valueOf(vtx0S[1]);
           vtx0.pnt.z=Double.valueOf(vtx0S[2]);

           vtx1.pnt.x=Double.valueOf(vtx1S[0]);
           vtx1.pnt.y=Double.valueOf(vtx1S[1]);
           vtx1.pnt.z=Double.valueOf(vtx1S[2]);

           
           
            ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
        
        
        String line = value.toString();
        
        if (line.length()>1){
        String []linesArr = line.split("\n");
        
        
        for (int i=0; i<linesArr.length;i++){
        
            String[] S=linesArr[i].split(" ");
          if (S.length>=3){
            Vertex TVertex=new Vertex();
            
            TVertex.pnt.x=Double.valueOf(S[0]);
            TVertex.pnt.y=Double.valueOf(S[1]);
            TVertex.pnt.z=Double.valueOf(S[2]);
            
            PointsArr.add(TVertex);
          }    
        }
        }
           
           
           
           if (PointsArr.size()>1){
           
             
	   for (int i=0; i<PointsArr.size(); i++)
               
	    { diff02.sub (PointsArr.get(i).pnt, vtx0.pnt);
	      xprod.cross (u01, diff02);
	      double lenSqr = xprod.normSquared();
	      if (lenSqr > maxSqr &&
		  PointsArr.get(i) != vtx0 &&  // paranoid
		  PointsArr.get(i) != vtx1)
	       { maxSqr = lenSqr; 
		 vtx2 = PointsArr.get(i);
		 nrml.set (xprod);
	       }
	    }
           
           
           TKey.set("Reducer1");
           context.write(TKey,new Text(vtx2.pnt.x+" "+vtx2.pnt.y+" "+vtx2.pnt.z));
           
        }else{
        
        context.write(TKey,new Text(PointsArr.get(0).pnt.toString()));
        
        
        }           
        
        
       
       }  
        
    }
     
       
      
 public static class MaxMin2Reducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   
    
    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
       
           
        ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
         
            for (Text val : values) {
                 String line = val.toString();
                 String []linesArr = line.split(" ");
                 Vertex TVertex=new Vertex();
                 TVertex.pnt.x=Double.valueOf(linesArr[0]);
                 TVertex.pnt.y=Double.valueOf(linesArr[1]);
                 TVertex.pnt.z=Double.valueOf(linesArr[2]);
            
                 PointsArr.add(TVertex);
         // context.write(null,new Text(TVertex.pnt.toString()));
            }
        
        
           Vector3d u01 = new Vector3d();
	   Vector3d diff02 = new Vector3d();
	   Vector3d nrml = new Vector3d();
	   Vector3d xprod = new Vector3d();
           Vertex vtx0=new Vertex();
           Vertex vtx1=new Vertex();
	   Vertex vtx2=new Vertex();
           double maxSqr = 0;
        
        
           Configuration conf = context.getConfiguration();
        
           String[] u01S=conf.get("u01S").split(" ");
           String[] vtx0S=conf.get("vtx0S").split(" ");
           String[] vtx1S=conf.get("vtx1S").split(" ");
           //String xprodS=xprod.toString();
           //String nrmlS=nrml.toString();
           
           u01.x=Double.valueOf(u01S[0]);
           u01.y=Double.valueOf(u01S[1]);
           u01.z=Double.valueOf(u01S[2]);
           
           vtx0.pnt.x=Double.valueOf(vtx0S[0]);
           vtx0.pnt.y=Double.valueOf(vtx0S[1]);
           vtx0.pnt.z=Double.valueOf(vtx0S[2]);

           vtx1.pnt.x=Double.valueOf(vtx1S[0]);
           vtx1.pnt.y=Double.valueOf(vtx1S[1]);
           vtx1.pnt.z=Double.valueOf(vtx1S[2]);

           
           
           
             
	   for (int i=0; i<PointsArr.size(); i++)
	    { diff02.sub (PointsArr.get(i).pnt, vtx0.pnt);
	      xprod.cross (u01, diff02);
	      double lenSqr = xprod.normSquared();
	      if (lenSqr > maxSqr &&
		  PointsArr.get(i) != vtx0 &&  // paranoid
		  PointsArr.get(i) != vtx1)
	       { maxSqr = lenSqr; 
		 vtx2 = PointsArr.get(i);
		 nrml.set (xprod);
	       }
	    }
           
       
           
           
           
           context.write(null,new Text("vtx2"+"_"+vtx2.pnt.x+" "+vtx2.pnt.y+" "+vtx2.pnt.z));
           context.write(null,new Text("maxsqr"+"_"+maxSqr));
           context.write(null,new Text("nrml"+"_"+nrml.toString()));
          // context.write(null,new Text("uo1"+"_"+u01.toString()));
          // context.write(null,new Text("vtx0"+"_"+vtx0.pnt.toString()));
          // context.write(null,new Text("vtx1"+"_"+vtx1.pnt.toString()));
        
        
        
        
        
        
   
    }
  }
     
/******************************************************************************/

  
 public static class MaxMin3Mapper 
       extends Mapper<Object, Text, Text, Text>{
    
    private Text TKey = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
        Configuration conf = context.getConfiguration();
        
        String d0S=conf.get("d0S");
        String MaxDistS=conf.get("MaxDistS");
        String[] nrmlS=conf.get("nrml").split(" ");
        
        double d0=Double.valueOf(d0S);
        double MaxDist=Double.valueOf(MaxDistS);
        Vector3d nrml = new Vector3d();
        
        Vertex vtx0=new Vertex();
        Vertex vtx1=new Vertex();
	Vertex vtx2=new Vertex();
        Vertex vtx3=new Vertex();
        
        String[] vtx0S=conf.get("vtx0S").split(" ");
        String[] vtx1S=conf.get("vtx1S").split(" ");
        String[] vtx2S=conf.get("vtx2S").split(" ");    
       
       vtx0.pnt.x=Double.valueOf(vtx0S[0]);
       vtx0.pnt.y=Double.valueOf(vtx0S[1]);
       vtx0.pnt.z=Double.valueOf(vtx0S[2]);

       vtx1.pnt.x=Double.valueOf(vtx1S[0]);
       vtx1.pnt.y=Double.valueOf(vtx1S[1]);
       vtx1.pnt.z=Double.valueOf(vtx1S[2]);

       vtx2.pnt.x=Double.valueOf(vtx2S[0]);
       vtx2.pnt.y=Double.valueOf(vtx2S[1]);
       vtx2.pnt.z=Double.valueOf(vtx2S[2]);

        
        
        
        
        
        nrml.x=Double.valueOf(nrmlS[0]);
        nrml.y=Double.valueOf(nrmlS[1]);
        nrml.z=Double.valueOf(nrmlS[2]);
        
        ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
        
        String line = value.toString();
        
        if (line.length()>1){
        String []linesArr = line.split("\n");
        
        
        for (int i=0; i<linesArr.length;i++){
        
            String[] S=linesArr[i].split(" ");
          if (S.length>=3){
            Vertex TVertex=new Vertex();
            
            TVertex.pnt.x=Double.valueOf(S[0]);
            TVertex.pnt.y=Double.valueOf(S[1]);
            TVertex.pnt.z=Double.valueOf(S[2]);
            
            PointsArr.add(TVertex);
          }    
        }
        
        
        
        
        
        
        for (int i=0; i< PointsArr.size(); i++)
	    { double dist = Math.abs ( PointsArr.get(i).pnt.dot(nrml) - d0);
	      if (dist > MaxDist &&
		   PointsArr.get(i) != vtx0 &&  // paranoid
		   PointsArr.get(i) != vtx1 &&
		   PointsArr.get(i)!= vtx2)
	       { MaxDist = dist;
		 vtx3 =  PointsArr.get(i);
	       }
	    }
	   
        
        
        context.write(new Text("Reducer1"), new Text (vtx3.pnt.toString()));
        
           
        
    }
 }
 }    
      
 public static class MaxMin3Reducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   
    
    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
       
        
       Configuration conf = context.getConfiguration();
        
       String d0S=conf.get("d0S");
       String MaxDistS=conf.get("MaxDistS");
       String[] nrmlS=conf.get("nrml").split(" ");
        
       double d0=Double.valueOf(d0S);
       double MaxDist=Double.valueOf(MaxDistS);
       Vector3d nrml = new Vector3d();
        
       Vertex vtx0=new Vertex();
       Vertex vtx1=new Vertex();
       Vertex vtx2=new Vertex();
       Vertex vtx3=new Vertex();
        
       String[] vtx0S=conf.get("vtx0S").split(" ");
       String[] vtx1S=conf.get("vtx1S").split(" ");
       String[] vtx2S=conf.get("vtx2S").split(" ");    
       
       vtx0.pnt.x=Double.valueOf(vtx0S[0]);
       vtx0.pnt.y=Double.valueOf(vtx0S[1]);
       vtx0.pnt.z=Double.valueOf(vtx0S[2]);

       vtx1.pnt.x=Double.valueOf(vtx1S[0]);
       vtx1.pnt.y=Double.valueOf(vtx1S[1]);
       vtx1.pnt.z=Double.valueOf(vtx1S[2]);

       vtx2.pnt.x=Double.valueOf(vtx2S[0]);
       vtx2.pnt.y=Double.valueOf(vtx2S[1]);
       vtx2.pnt.z=Double.valueOf(vtx2S[2]);

        
        
        
        
        
        nrml.x=Double.valueOf(nrmlS[0]);
        nrml.y=Double.valueOf(nrmlS[1]);
        nrml.z=Double.valueOf(nrmlS[2]);
        
        
        ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
         
            for (Text val : values) {
                 String line = val.toString();
                 String []linesArr = line.split(" ");
                 Vertex TVertex=new Vertex();
                 TVertex.pnt.x=Double.valueOf(linesArr[0]);
                 TVertex.pnt.y=Double.valueOf(linesArr[1]);
                 TVertex.pnt.z=Double.valueOf(linesArr[2]);
            
                 PointsArr.add(TVertex);
         // context.write(null,new Text(TVertex.pnt.toString()));
            }
        
        

        
        for (int i=0; i< PointsArr.size(); i++)
	    { double dist = Math.abs ( PointsArr.get(i).pnt.dot(nrml) - d0);
	      if (dist > MaxDist &&
		   PointsArr.get(i) != vtx0 &&  // paranoid
		   PointsArr.get(i) != vtx1 &&
		   PointsArr.get(i)!= vtx2)
	       { MaxDist = dist;
		 vtx3 =  PointsArr.get(i);
	       }
	    }
	   
        
        
        context.write(null, new Text ("vtx3S"+"_"+vtx3.pnt.toString()));
        context.write(null, new Text ("MaxDistS"+"_"+String.valueOf(MaxDist)));
        
        
        
  }

 }
 
 
/******************************************************************************/
 
 
  
 public static class AssignP1Mapper 
       extends Mapper<Object, Text, Text, Text>{
    
    private Text TKey = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
       if ((value!=null)&&(value.toString().isEmpty()==false)){ 
        
        Configuration conf = context.getConfiguration();
        
        String FS=conf.get("FacesS");
        String toleranceS=conf.get("toleranceS");
        double tolerance=Double.valueOf(toleranceS);
        
       String[] FacesInfo=FS.split("_");
       
       
             String[] lineArray=value.toString().split(" ");
	     Vertex v = new Vertex();
             if ((lineArray[0].isEmpty()==false)&&(lineArray[1].isEmpty()==false)&&(lineArray[2].isEmpty()==false))
             {
             v.pnt.x=Double.valueOf(lineArray[0]);
             v.pnt.y=Double.valueOf(lineArray[1]);
             v.pnt.z=Double.valueOf(lineArray[2]);
            }
	     
	      double maxDist = tolerance;
	      String maxFace = null;
	      
              for (int k=0; k<4; k++)
	       { 
                 String[] FaceInfo=FacesInfo[k].split(" ");  
                 String FName=FaceInfo[0];
                 
                // context.write(new Text("Reducer1"), new Text ("FName"+k+FaceInfo[0]+">>"+FaceInfo[1]+">>"+FaceInfo[2]+">>"+FaceInfo[3]+">>"+FaceInfo[4]));
                 
               
                 double Fnrmlx=Double.valueOf(FaceInfo[1]);
                 double Fnrmly=Double.valueOf(FaceInfo[2]);
                 double Fnrmlz=Double.valueOf(FaceInfo[3]);
                 double FPO=Double.valueOf(FaceInfo[4]);
                 
                 
                 
                   
                 double dist = Fnrmlx*v.pnt.x + Fnrmly*v.pnt.y + Fnrmlz*v.pnt.z - FPO;
		 if (dist > maxDist)
		  { maxFace = FName;
		    maxDist = dist;
		  }
	       }
	      
              
              if (maxFace != null)
	       { 
                   
                   //context.write(new Text("Reducer1"),new Text(maxFace+"_"+v.pnt.toString()));
                   context.write(new Text(maxFace),new Text(maxFace+"_"+v.pnt.toString()));
	       }	      
	    
       
       
       
       
       
     
       }   
           
      
    
 }
 }    
      
 public static class AssignP1Reducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   
    
    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
       
        
        
        
        
        for (Text val:values){
        
        context.write(null, val);
        }
        
        
  }

 }
 
 
 
 
/******************************************************************************/
 
  
 public static class AssignP2Mapper 
       extends Mapper<Object, Text, Text, Text>{
    
    private Text TKey = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
       boolean flag=false;
       boolean  HF=false;
       String[] HorizonFS;
       
       if ((value!=null)&&(value.toString().replace("\n","").isEmpty()==false)){ 
        
        Configuration conf = context.getConfiguration();
        
        String FS=conf.get("FacesS");
        HorizonFS=conf.get("HorizonFS").split("_");
        
        String toleranceS=conf.get("toleranceS");
        
        double tolerance=Double.valueOf(toleranceS);
        
       String[] FacesInfo=FS.split("_");
       
             String[] RecordArray=value.toString().split("_");
             String[] lineArray=RecordArray[1].toString().split(" ");
	     Vertex v = new Vertex();
             if ((lineArray[0].isEmpty()==false)&&(lineArray[1].isEmpty()==false)&&(lineArray[2].isEmpty()==false))
             {
             v.pnt.x=Double.valueOf(lineArray[0]);
             v.pnt.y=Double.valueOf(lineArray[1]);
             v.pnt.z=Double.valueOf(lineArray[2]);
            }
	     
             for(int i=0;i<HorizonFS.length;i++){
             
             if (HorizonFS[i].equalsIgnoreCase(RecordArray[0])){
             
             HF=true;
             break;
             }
             
             }
             
             
             if(HF=true){
             
	      double maxDist = tolerance;
	      String maxFace = null;
	      
              for (int k=0; k<FacesInfo.length; k++)
	       { 
                 String[] FaceInfo=FacesInfo[k].split(" ");  
                 String FName=FaceInfo[0];
                 
                // context.write(new Text("Reducer1"), new Text ("FName"+k+FaceInfo[0]+">>"+FaceInfo[1]+">>"+FaceInfo[2]+">>"+FaceInfo[3]+">>"+FaceInfo[4]));
                 
               
                 double Fnrmlx=Double.valueOf(FaceInfo[1]);
                 double Fnrmly=Double.valueOf(FaceInfo[2]);
                 double Fnrmlz=Double.valueOf(FaceInfo[3]);
                 double FPO=Double.valueOf(FaceInfo[4]);
                 Vertex FV1=new Vertex();
                 Vertex FV2=new Vertex();
                 Vertex FV3=new Vertex();
                 FV1.pnt.x=Double.valueOf(FaceInfo[5]);
                 FV1.pnt.y=Double.valueOf(FaceInfo[6]);
                 FV1.pnt.z=Double.valueOf(FaceInfo[7]);
                 FV2.pnt.x=Double.valueOf(FaceInfo[8]);
                 FV2.pnt.y=Double.valueOf(FaceInfo[9]);
                 FV2.pnt.z=Double.valueOf(FaceInfo[10]);
                 FV3.pnt.x=Double.valueOf(FaceInfo[11]);
                 FV3.pnt.y=Double.valueOf(FaceInfo[12]);
                 FV3.pnt.z=Double.valueOf(FaceInfo[13]);
                 
                 if ((v.pnt.x==FV1.pnt.x)&&(v.pnt.y==FV1.pnt.y)&&(v.pnt.z==FV1.pnt.z)){
                 flag=true;
                 break;
                 }
                 
                 if ((v.pnt.x==FV2.pnt.x)&&(v.pnt.y==FV2.pnt.y)&&(v.pnt.z==FV2.pnt.z)){
                 flag=true;
                 break;
                 }
                 
                 if ((v.pnt.x==FV3.pnt.x)&&(v.pnt.y==FV3.pnt.y)&&(v.pnt.z==FV3.pnt.z)){
                 flag=true;
                 break;
                 }
                 
                 
                 
                 double dist = Fnrmlx*v.pnt.x + Fnrmly*v.pnt.y + Fnrmlz*v.pnt.z - FPO;
		 if (dist > maxDist)
		  { maxFace = FName;
		    maxDist = dist;
		  }
	       
               if (maxDist > 1000*tolerance)
		     { break;
		     }
               
               }
	      
              
              if ((maxFace != null)&&(flag==false))
	       { 
                   
                   //context.write(new Text("Reducer1"),new Text(maxFace+"_"+v.pnt.toString()));
	       context.write(new Text(maxFace),new Text(maxFace+"_"+v.pnt.toString()));
               }	      
	    
       
       
       
       
      
     
       }
             else{
                 
             //    context.write(new Text("Reducer1"),value);
                 context.write(new Text( RecordArray[0]),value);
             }
           
      
       }  
 }
 }    
      
 public static class AssignP2Reducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   
    
    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
       
        
        
        for (Text val:values){
        context.write(null, val);
        
        }
        
        
  }

 }
 
 
  
/******************************************************************************/ 
 public static class SortMapper 
       extends Mapper<Object, Text, LongWritable, Text>{
    
    Text Key=new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
       if ((value!=null)&&(value.toString().isEmpty()==false)){ 
        
           
        String[] lineArray=value.toString().split("_");
        
        long TempInt=Long.valueOf(lineArray[0]);
        Key=new Text(String.valueOf(1372036854-TempInt));
        
        //context.write(new LongWritable (1372036854-TempInt),value);
       
        context.write(new LongWritable (TempInt),value);
       }   
           
      
    
 }
 }    
      
 public static class SortReducer 
       extends Reducer<LongWritable,Text,NullWritable,Text> {
   
    
    public void reduce(LongWritable key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
        for (Text val:values){
            if ((val!=null)&&(val.toString().isEmpty()==false)){
       context.write(null,new Text(val.toString().trim() ));}
        
        }
  }

 }
 
 
/******************************************************************************/
 
  
 public static class FindFPMapper 
       extends Mapper<Object, Text, Text, Text>{
    
    
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
       if ((value!=null)&&(value.toString().isEmpty()==false)){ 
        
        Vertex EyeVertex=null;
        String FaceName=null;
        
        Configuration conf = context.getConfiguration();
        
        String[] Faceinfo=conf.get("Faceinfo").split(" ");
        FaceName=conf.get("FaceName");
        
        //context.write(new Text("Reduce1"), new Text("FaceN>>"+FaceName));
        
        ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
        
        String line = value.toString();
        
        if (line.length()>1){
        String []linesArr = line.split("\n");
        
        
        for (int i=0; i<linesArr.length;i++){
        
            String[] S=linesArr[i].split("_");
          
         //context.write(new Text("Reduce1"), new Text("S[0]>>"+S[0]));
         
         //context.write(new Text("Reduce1"), new Text("S[1]>>"+S[1]));  
            
          if (S.length>=2){
            Vertex TVertex=new Vertex();
            
            if (S[0].equalsIgnoreCase(FaceName)==true){
            
            String[] SS=S[1].split(" ");
            TVertex.pnt.x=Double.valueOf(SS[0]);
            TVertex.pnt.y=Double.valueOf(SS[1]);
            TVertex.pnt.z=Double.valueOf(SS[2]);
            PointsArr.add(TVertex);
            //context.write(new Text("Reduce1"), new Text("PointsArr>>"+TVertex.pnt.toString()));
            }
          }    
        }
        
           
        
        
        
        Vector3d Fnrml=new Vector3d();
        Fnrml.x=Double.valueOf(Faceinfo[0]);
        Fnrml.y=Double.valueOf(Faceinfo[1]);
        Fnrml.z=Double.valueOf(Faceinfo[2]);
        double FPO=Double.valueOf(Faceinfo[3]);
        
        //context.write(new Text("Reduce1"), new Text("Fnrml>>"+Fnrml.toString()+">>"+FPO));
        
        double maxDist = 0;
        for (int i=0;i<PointsArr.size();i++){
            
           double dist = Fnrml.x*PointsArr.get(i).pnt.x + Fnrml.y*PointsArr.get(i).pnt.y + Fnrml.z*PointsArr.get(i).pnt.z - FPO;
		 
              if (dist > maxDist)
		  { maxDist = dist;
		     EyeVertex=new Vertex();
                     EyeVertex =PointsArr.get(i);
		  }
        
        
        }
        
        
        
        
        
           
           
           
           
       }   
           
      if (EyeVertex!=null){
      context.write(new Text ("Reducer1"), new Text(EyeVertex.pnt.toString()));
      }
    
 }
 }
 }
      
 public static class FindFPReducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   
    
    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
        
        ArrayList<Vertex> PointsArr = new ArrayList<Vertex>();
         
            for (Text val : values) {
                 String line = val.toString();
                 String []linesArr = line.split(" ");
                 Vertex TVertex=new Vertex();
                 TVertex.pnt.x=Double.valueOf(linesArr[0]);
                 TVertex.pnt.y=Double.valueOf(linesArr[1]);
                 TVertex.pnt.z=Double.valueOf(linesArr[2]);
            
                 PointsArr.add(TVertex);
          
            }

            
        Vertex EyeVertex=null;
        String FaceName=null;
        
        Configuration conf = context.getConfiguration();
        
        String[] Faceinfo=conf.get("Faceinfo").split(" ");
        FaceName=conf.get("FaceName");
        
            
          
        Vector3d Fnrml=new Vector3d();
        Fnrml.x=Double.valueOf(Faceinfo[0]);
        Fnrml.y=Double.valueOf(Faceinfo[1]);
        Fnrml.z=Double.valueOf(Faceinfo[2]);
        double FPO=Double.valueOf(Faceinfo[3]);
        
        //context.write(new Text("Reduce1"), new Text("Fnrml>>"+Fnrml.toString()+">>"+FPO));
        
        double maxDist = 0;
        for (int i=0;i<PointsArr.size();i++){
            
           double dist = Fnrml.x*PointsArr.get(i).pnt.x + Fnrml.y*PointsArr.get(i).pnt.y + Fnrml.z*PointsArr.get(i).pnt.z - FPO;
		 
              if (dist > maxDist)
		  { maxDist = dist;
		     EyeVertex=new Vertex();
                     EyeVertex =PointsArr.get(i);
		  }
        }
             
           
      if (EyeVertex!=null){
      context.write(null, new Text(EyeVertex.pnt.toString()));
      }
   

 }
 
 }
 
/******************************************************************************/
 
        
 public static class QHMapper 
       extends Mapper<Object, Text, Text, Text>{
    
 /*****************************************************************************/
     	/**
	 * Specifies that (on output) vertex indices for a face should be
	 * listed in clockwise order.
	 */
	public static final int CLOCKWISE = 0x1;

	/**
	 * Specifies that (on output) the vertex indices for a face should be
	 * numbered starting from 1.
	 */
	public static final int INDEXED_FROM_ONE = 0x2;

	/**
	 * Specifies that (on output) the vertex indices for a face should be
	 * numbered starting from 0.
	 */
	public static final int INDEXED_FROM_ZERO = 0x4;

	/**
	 * Specifies that (on output) the vertex indices for a face should be
	 * numbered with respect to the original input points.
	 */
	public static final int POINT_RELATIVE = 0x8;

	/**
	 * Specifies that the distance tolerance should be
	 * computed automatically from the input point data.
	 */
	public static final double AUTOMATIC_TOLERANCE = -1;

	protected int findIndex = -1;

	// estimated size of the point set
	protected double charLength;

	protected boolean debug = false;

	protected Vertex[] pointBuffer = new Vertex[0];
	protected int[] vertexPointIndices = new int[0];
	private Face[] discardedFaces = new Face[3];

	private Vertex[] maxVtxs = new Vertex[3];
	private Vertex[] minVtxs = new Vertex[3];

	protected ArrayList<Face> faces = new ArrayList<Face>(16);
	protected Vector horizon = new Vector(16);

	private FaceList newFaces = new FaceList();
	private VertexList unclaimed = new VertexList();
	private VertexList claimed = new VertexList();
        ArrayList<Face>TempC=new ArrayList<Face>();
        
	protected int numVertices;
	protected int numFaces;
	protected int numPoints;

	protected double explicitTolerance = AUTOMATIC_TOLERANCE;
	protected double tolerance;

	/**
	 * Returns true if debugging is enabled.
	 *
	 * @return true is debugging is enabled
	 * @see QuickHull3D#setDebug
	 */
	public boolean getDebug()
	 {
	   return debug;
	 }

	/**
	 * Enables the printing of debugging diagnostics.
	 *
	 * @param enable if true, enables debugging
	 */
	public void setDebug (boolean enable)
	 { 
	   debug = enable;
	 }

	/**
	 * Precision of a double.
	 */
	static private final double DOUBLE_PREC = 2.2204460492503131e-16;


	/**
	 * Returns the distance tolerance that was used for the most recently
	 * computed hull. The distance tolerance is used to determine when
	 * faces are unambiguously convex with respect to each other, and when
	 * points are unambiguously above or below a face plane, in the
	 * presence of <a href=#distTol>numerical imprecision</a>. Normally,
	 * this tolerance is computed automatically for each set of input
	 * points, but it can be set explicitly by the application.
	 *
	 * @return distance tolerance
	 * @see QuickHull3D#setExplicitDistanceTolerance
	 */
	public double getDistanceTolerance()
	 {
	   return tolerance;
	 }

	/**
	 * Sets an explicit distance tolerance for convexity tests.
	 * If {@link #AUTOMATIC_TOLERANCE AUTOMATIC_TOLERANCE}
	 * is specified (the default), then the tolerance will be computed
	 * automatically from the point data.
	 *
	 * @param tol explicit tolerance
	 * @see #getDistanceTolerance
	 */
	public void setExplicitDistanceTolerance(double tol)
	 { 
	   explicitTolerance = tol;
	 }

	/**
	 * Returns the explicit distance tolerance.
	 *
	 * @return explicit tolerance
	 * @see #setExplicitDistanceTolerance
	 */
	public double getExplicitDistanceTolerance()
	 {
	   return explicitTolerance;
	 }

	private void addPointToFace (Vertex vtx, Face face)
	 {
	   vtx.face = face;

	   if (face.outside == null)
	    { claimed.add (vtx);
	    }
	   else
	    { claimed.insertBefore (vtx, face.outside); 
	    }
	   face.outside = vtx;
	 }

	private void removePointFromFace (Vertex vtx, Face face)
	 {
	   if (vtx == face.outside)
	    { if (vtx.next != null && vtx.next.face == face)
	       { face.outside = vtx.next;
	       }
	      else
	       { face.outside = null; 
	       }
	    }
	   claimed.delete (vtx);
	 }

	private Vertex removeAllPointsFromFace (Face face)
	 {
	   if (face.outside != null)
	    { 
	      Vertex end = face.outside;
	      while (end.next != null && end.next.face == face)
	       { end = end.next;
	       }
	      claimed.delete (face.outside, end);
	      end.next = null;
	      return face.outside;
	    }
	   else
	    { return null; 
	    }
	 }

	

	
	private HalfEdge findHalfEdge (Vertex tail, Vertex head)
	 { 
	   // brute force ... OK, since setHull is not used much
	   for (Iterator it=faces.iterator(); it.hasNext(); ) 
	    { HalfEdge he = ((Face)it.next()).findEdge (tail, head);
	      if (he != null)
	       { return he; 
	       }
	    }
	   return null;
	 }

 	protected void setHull (double[] coords, int nump,
				int[][] faceIndices, int numf)
 	 {
 	   initBuffers (nump);
	   setPoints (coords, nump);
	   computeMaxAndMin ();
	   for (int i=0; i<numf; i++)
	    { Face face = Face.create (pointBuffer, faceIndices[i]);
	      HalfEdge he = face.he0;
	      do
	       { HalfEdge heOpp = findHalfEdge (he.head(), he.tail());
		 if (heOpp != null)
		  { he.setOpposite (heOpp); 
		  }
		 he = he.next;
	       }
	      while (he != face.he0);
	      faces.add (face);
	    }
 	 }

	private void printQhullErrors (Process proc)
	   throws IOException
	 {
	   boolean wrote = false;
	   InputStream es = proc.getErrorStream();
	   while (es.available() > 0)
	    { System.out.write (es.read());
	      wrote = true;
	    }
	   if (wrote)
	    { System.out.println("");
	    }
	 }

	protected void setFromQhull (double[] coords, int nump,
				     boolean triangulate)
	 {
	   String commandStr = "./qhull i";
	   if (triangulate)
	    { commandStr += " -Qt"; 
	    }
	   try
	    { 
	      Process proc = Runtime.getRuntime().exec (commandStr);
	      PrintStream ps = new PrintStream (proc.getOutputStream());
	      StreamTokenizer stok =
		 new StreamTokenizer (
		    new InputStreamReader (proc.getInputStream()));

	      ps.println ("3 " + nump);
	      for (int i=0; i<nump; i++)
	       { ps.println (
		    coords[i*3+0] + " " +
		    coords[i*3+1] + " " +  
		    coords[i*3+2]);
	       }
	      ps.flush();
	      ps.close();
	      Vector indexList = new Vector(3);
	      stok.eolIsSignificant(true);
	      printQhullErrors (proc);
	      
	      do
	       { stok.nextToken();
	       }
	      while (stok.sval == null ||
		     !stok.sval.startsWith ("MERGEexact"));
	      for (int i=0; i<4; i++)
	       { stok.nextToken();
	       }
	      if (stok.ttype != StreamTokenizer.TT_NUMBER)
	       { System.out.println ("Expecting number of faces");
		 System.exit(1); 
	       }
	      int numf = (int)stok.nval;
	      stok.nextToken(); // clear EOL
	      int[][] faceIndices = new int[numf][];
	      for (int i=0; i<numf; i++)
	       { indexList.clear();
		 while (stok.nextToken() != StreamTokenizer.TT_EOL)
		  { if (stok.ttype != StreamTokenizer.TT_NUMBER)
		     { System.out.println ("Expecting face index");
		       System.exit(1); 
		     }
		    indexList.add (0, new Integer((int)stok.nval));
		  }
		 faceIndices[i] = new int[indexList.size()];
		 int k = 0;
		 for (Iterator it=indexList.iterator(); it.hasNext(); ) 
		  { faceIndices[i][k++] = ((Integer)it.next()).intValue();
		  }
	       }
	      setHull (coords, nump, faceIndices, numf);
	    }
	   catch (Exception e) 
	    { e.printStackTrace();
	      System.exit(1); 
	    }
	 }

	private void printPoints (PrintStream ps)
	 {
	   for (int i=0; i<numPoints; i++)
	    { Point3d pnt = pointBuffer[i].pnt;
	      ps.println (pnt.x + ", " + pnt.y + ", " + pnt.z + ",");
	    }
	 }

	/**
	 * Constructs the convex hull of a set of points whose
	 * coordinates are given by an array of doubles.
	 *
	 * @param coords x, y, and z coordinates of each input
	 * point. The length of this array will be three times
	 * the number of input points.
	 * @throws IllegalArgumentException the number of input points is less
	 * than four, or the points appear to be coincident, colinear, or
	 * coplanar.
	 */
	public void build (double[] coords)
	   throws IllegalArgumentException
	 {
	   build (coords, coords.length/3);
	 }

	/**
	 * Constructs the convex hull of a set of points whose
	 * coordinates are given by an array of doubles.
	 *
	 * @param coords x, y, and z coordinates of each input
	 * point. The length of this array must be at least three times
	 * <code>nump</code>.
	 * @param nump number of input points
	 * @throws IllegalArgumentException the number of input points is less
	 * than four or greater than 1/3 the length of <code>coords</code>,
	 * or the points appear to be coincident, colinear, or
	 * coplanar.
	 */
	public void build (double[] coords, int nump)
	   throws IllegalArgumentException
	 {
	   if (nump < 4)
	    { throw new IllegalArgumentException (
		 "Less than four input points specified");
	    }
	   if (coords.length/3 < nump)
	    { throw new IllegalArgumentException (
		 "Coordinate array too small for specified number of points"); 
	    }
	   initBuffers (nump);
	   setPoints (coords, nump);
	   buildHull ();
	 }

	/**
	 * Constructs the convex hull of a set of points.
	 *
	 * @param points input points
	 * @throws IllegalArgumentException the number of input points is less
	 * than four, or the points appear to be coincident, colinear, or
	 * coplanar.
	 */
	public void build (Point3d[] points)
	   throws IllegalArgumentException
	 {
	   build (points, points.length);
	 }

	/**
	 * Constructs the convex hull of a set of points.
	 *
	 * @param points input points
	 * @param nump number of input points
	 * @throws IllegalArgumentException the number of input points is less
	 * than four or greater then the length of <code>points</code>, or the
	 * points appear to be coincident, colinear, or coplanar.
	 */
	public void build (Point3d[] points, int nump)
	   throws IllegalArgumentException
	 {
	   if (nump < 4)
	    { throw new IllegalArgumentException (
		 "Less than four input points specified");
	    }
	   if (points.length < nump)
	    { throw new IllegalArgumentException (
		 "Point array too small for specified number of points"); 
	    }
	   initBuffers (nump);
	   setPoints (points, nump);
	   buildHull ();
	 }

	/**
	 * Triangulates any non-triangular hull faces. In some cases, due to
	 * precision issues, the resulting triangles may be very thin or small,
	 * and hence appear to be non-convex (this same limitation is present
	 * in <a href=http://www.qhull.org>qhull</a>).
	 */
	public void triangulate ()
	 {
	   double minArea = 1000*charLength*DOUBLE_PREC;
	   newFaces.clear();
	   for (Iterator it=faces.iterator(); it.hasNext(); ) 
	    { Face face = (Face)it.next();
	      if (face.mark == Face.VISIBLE)
	       { 
		 face.triangulate (newFaces, minArea);
		 // splitFace (face);
	       }
	    }
	   for (Face face=newFaces.first(); face!=null; face=face.next)
	    { faces.add (face);
	    }
	 }

	protected void initBuffers (int nump)
	 {
	   if (pointBuffer.length < nump)
	    { Vertex[] newBuffer = new Vertex[nump];
	      vertexPointIndices = new int[nump];
	      for (int i=0; i<pointBuffer.length; i++)
	       { newBuffer[i] = pointBuffer[i]; 
	       }
	      for (int i=pointBuffer.length; i<nump; i++)
	       { newBuffer[i] = new Vertex(); 
	       }
	      pointBuffer = newBuffer;
	    }
	   faces.clear();
	   claimed.clear();
	   numFaces = 0;
	   numPoints = nump;
	 }

	protected void setPoints (double[] coords, int nump)
	 { 
	   for (int i=0; i<nump; i++)
	    { 
	      Vertex vtx = pointBuffer[i];
	      vtx.pnt.set (coords[i*3+0], coords[i*3+1], coords[i*3+2]);
	      vtx.index = i;
	    }
	 }

	protected void setPoints (Point3d[] pnts, int nump)
	 { 
	   for (int i=0; i<nump; i++)
	    { 
	      Vertex vtx = pointBuffer[i];
	      vtx.pnt.set (pnts[i]);
	      vtx.index = i;
	    }
	 }

	protected void computeMaxAndMin ()
	 {
	   Vector3d max = new Vector3d();
	   Vector3d min = new Vector3d();

	   for (int i=0; i<3; i++)
	    { maxVtxs[i] = minVtxs[i] = pointBuffer[0]; 
	    }
	   max.set (pointBuffer[0].pnt);
	   min.set (pointBuffer[0].pnt);

	   for (int i=1; i<numPoints; i++)
	    { Point3d pnt = pointBuffer[i].pnt;
	      if (pnt.x > max.x)
	       { max.x = pnt.x;
		 maxVtxs[0] = pointBuffer[i];
	       }
	      else if (pnt.x < min.x)
	       { min.x = pnt.x;
		 minVtxs[0] = pointBuffer[i];
	       }
	      if (pnt.y > max.y)
	       { max.y = pnt.y;
		 maxVtxs[1] = pointBuffer[i];
	       }
	      else if (pnt.y < min.y)
	       { min.y = pnt.y;
		 minVtxs[1] = pointBuffer[i];
	       }
	      if (pnt.z > max.z)
	       { max.z = pnt.z;
		 maxVtxs[2] = pointBuffer[i];
	       }
	      else if (pnt.z < min.z)
	       { min.z = pnt.z;
		 minVtxs[2] = pointBuffer[i];
	       }
	    }

	   // this epsilon formula comes from QuickHull, and I'm
	   // not about to quibble.
	   // We use the charLength at the triangulate function
           charLength = Math.max(max.x-min.x, max.y-min.y);
	   charLength = Math.max(max.z-min.z, charLength);
	   if (explicitTolerance == AUTOMATIC_TOLERANCE)
	    { tolerance =
		 3*DOUBLE_PREC*(Math.max(Math.abs(max.x),Math.abs(min.x))+
				Math.max(Math.abs(max.y),Math.abs(min.y))+
				Math.max(Math.abs(max.z),Math.abs(min.z)));
	    }
	   else
	    { tolerance = explicitTolerance; 
	    }
	 }

	/**
	 * Creates the initial simplex from which the hull will be built.
	 */
	protected void createInitialSimplex ()
	   throws IllegalArgumentException
	 {
	   double max = 0;
	   int imax = 0;

	   for (int i=0; i<3; i++)
	    { double diff = maxVtxs[i].pnt.get(i)-minVtxs[i].pnt.get(i);
	      if (diff > max)
	       { max = diff;
		 imax = i;
	       }
 	    }

	   if (max <= tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be coincident");
	    }
	   Vertex[] vtx = new Vertex[4];
	   // set first two vertices to be those with the greatest
	   // one dimensional separation

	   vtx[0] = maxVtxs[imax];
	   vtx[1] = minVtxs[imax];

	   // set third vertex to be the vertex farthest from
	   // the line between vtx0 and vtx1
	   Vector3d u01 = new Vector3d();
	   Vector3d diff02 = new Vector3d();
	   Vector3d nrml = new Vector3d();
	   Vector3d xprod = new Vector3d();
	   double maxSqr = 0;
	   u01.sub (vtx[1].pnt, vtx[0].pnt);
	   u01.normalize();
	   for (int i=0; i<numPoints; i++)
	    { diff02.sub (pointBuffer[i].pnt, vtx[0].pnt);
	      xprod.cross (u01, diff02);
	      double lenSqr = xprod.normSquared();
	      if (lenSqr > maxSqr &&
		  pointBuffer[i] != vtx[0] &&  // paranoid
		  pointBuffer[i] != vtx[1])
	       { maxSqr = lenSqr; 
		 vtx[2] = pointBuffer[i];
		 nrml.set (xprod);
	       }
	    }
	   if (Math.sqrt(maxSqr) <= 100*tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be colinear");
	    }
	   nrml.normalize();


	   double maxDist = 0;
	   double d0 = vtx[2].pnt.dot (nrml);
	   for (int i=0; i<numPoints; i++)
	    { double dist = Math.abs (pointBuffer[i].pnt.dot(nrml) - d0);
	      if (dist > maxDist &&
		  pointBuffer[i] != vtx[0] &&  // paranoid
		  pointBuffer[i] != vtx[1] &&
		  pointBuffer[i] != vtx[2])
	       { maxDist = dist;
		 vtx[3] = pointBuffer[i];
	       }
	    }
	   if (Math.abs(maxDist) <= 100*tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be coplanar"); 
	    }

	   if (debug)
	    { System.out.println ("initial vertices:");
	      System.out.println (vtx[0].index + ": " + vtx[0].pnt);
	      System.out.println (vtx[1].index + ": " + vtx[1].pnt);
	      System.out.println (vtx[2].index + ": " + vtx[2].pnt);
	      System.out.println (vtx[3].index + ": " + vtx[3].pnt);
	    }

	   Face[] tris = new Face[4];

	   if (vtx[3].pnt.dot (nrml) - d0 < 0)
	    { tris[0] = Face.createTriangle (vtx[0], vtx[1], vtx[2]);
	      tris[1] = Face.createTriangle (vtx[3], vtx[1], vtx[0]);
	      tris[2] = Face.createTriangle (vtx[3], vtx[2], vtx[1]);
	      tris[3] = Face.createTriangle (vtx[3], vtx[0], vtx[2]);

	      for (int i=0; i<3; i++)
	       { int k = (i+1)%3;
		 tris[i+1].getEdge(1).setOpposite (tris[k+1].getEdge(0));
		 tris[i+1].getEdge(2).setOpposite (tris[0].getEdge(k));
	       }
	    }
	   else
	    { tris[0] = Face.createTriangle (vtx[0], vtx[2], vtx[1]);
	      tris[1] = Face.createTriangle (vtx[3], vtx[0], vtx[1]);
	      tris[2] = Face.createTriangle (vtx[3], vtx[1], vtx[2]);
	      tris[3] = Face.createTriangle (vtx[3], vtx[2], vtx[0]);

	      for (int i=0; i<3; i++)
	       { int k = (i+1)%3;
		 tris[i+1].getEdge(0).setOpposite (tris[k+1].getEdge(1));
		 tris[i+1].getEdge(2).setOpposite (tris[0].getEdge((3-i)%3));
	       }
	    }


 	   for (int i=0; i<4; i++)
 	    { faces.add (tris[i]); 
 	    }

	   for (int i=0; i<numPoints; i++)
	    { Vertex v = pointBuffer[i];

	      if (v == vtx[0] || v == vtx[1] || v == vtx[2] || v == vtx[3])
	       { continue;
	       }

	      maxDist = tolerance;
	      Face maxFace = null;
	      for (int k=0; k<4; k++)
	       { double dist = tris[k].distanceToPlane (v.pnt);
		 if (dist > maxDist)
		  { maxFace = tris[k];
		    maxDist = dist;
		  }
	       }
	      if (maxFace != null)
	       { addPointToFace (v, maxFace);
	       }	      
	    }
	 }

	/**
	 * Returns the number of vertices in this hull.
	 *
	 * @return number of vertices
	 */
	public int getNumVertices()
	 {
	   return numVertices;
	 }

	/**
	 * Returns the vertex points in this hull.
	 *
	 * @return array of vertex points
	 * @see QuickHull3D#getVertices(double[])
	 * @see QuickHull3D#getFaces()
	 */
 	public Point3d[] getVertices()
 	 {
 	   Point3d[] vtxs = new Point3d[numVertices];
 	   for (int i=0; i<numVertices; i++)
	    { vtxs[i] = pointBuffer[vertexPointIndices[i]].pnt;
	    }
	   return vtxs;
	 }

	/**
	 * Returns the coordinates of the vertex points of this hull.
	 *
	 * @param coords returns the x, y, z coordinates of each vertex.
	 * This length of this array must be at least three times
	 * the number of vertices.
	 * @return the number of vertices
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces()
	 */
 	public int getVertices(double[] coords)
 	 {
 	   for (int i=0; i<numVertices; i++)
	    { Point3d pnt = pointBuffer[vertexPointIndices[i]].pnt;
	      coords[i*3+0] = pnt.x;
	      coords[i*3+1] = pnt.y;
	      coords[i*3+2] = pnt.z;
	    }
	   return numVertices;
	 }

	/**
	 * Returns an array specifing the index of each hull vertex
	 * with respect to the original input points.
	 *
	 * @return vertex indices with respect to the original points
	 */
	public int[] getVertexPointIndices()
	 { 
	   int[] indices = new int[numVertices];
	   for (int i=0; i<numVertices; i++)
	    { indices[i] = vertexPointIndices[i];
	    }
	   return indices;
	 }

	/**
	 * Returns the number of faces in this hull.
	 *
	 * @return number of faces
	 */
	public int getNumFaces()
	 { 
	   return faces.size();
	 }

	/**
	 * Returns the faces associated with this hull.
	 *
	 * <p>Each face is represented by an integer array which gives the
	 * indices of the vertices. These indices are numbered
	 * relative to the
	 * hull vertices, are zero-based,
	 * and are arranged counter-clockwise. More control
	 * over the index format can be obtained using
	 * {@link #getFaces(int) getFaces(indexFlags)}.
	 *
	 * @return array of integer arrays, giving the vertex
	 * indices for each face.
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces(int)
	 */
	public int[][] getFaces ()
	 {
	   return getFaces(0);
	 }

	/**
	 * Returns the faces associated with this hull.
	 *
	 * <p>Each face is represented by an integer array which gives the
	 * indices of the vertices. By default, these indices are numbered with
	 * respect to the hull vertices (as opposed to the input points), are
	 * zero-based, and are arranged counter-clockwise. However, this
	 * can be changed by setting {@link #POINT_RELATIVE
	 * POINT_RELATIVE}, {@link #INDEXED_FROM_ONE INDEXED_FROM_ONE}, or
	 * {@link #CLOCKWISE CLOCKWISE} in the indexFlags parameter.
	 *
	 * @param indexFlags specifies index characteristics (0 results
	 * in the default)
	 * @return array of integer arrays, giving the vertex
	 * indices for each face.
	 * @see QuickHull3D#getVertices()
	 */
	public int[][] getFaces (int indexFlags)
	 {
	   int[][] allFaces = new int[faces.size()][];
	   int k = 0;
	   for (Iterator it=faces.iterator(); it.hasNext(); )
	    { Face face = (Face)it.next();
	      allFaces[k] = new int[face.numVertices()];
	      getFaceIndices (allFaces[k], face, indexFlags);
	      k++;
	    }
	   return allFaces;
	 }

	/**
	 * Prints the vertices and faces of this hull to the stream ps.
	 *
	 * <p>
	 * This is done using the Alias Wavefront .obj file
	 * format, with the vertices printed first (each preceding by
	 * the letter <code>v</code>), followed by the vertex indices
	 * for each face (each
	 * preceded by the letter <code>f</code>).
	 *
	 * <p>The face indices are numbered with respect to the hull vertices
	 * (as opposed to the input points), with a lowest index of 1, and are
	 * arranged counter-clockwise. More control over the index format can
	 * be obtained using
	 * {@link #print(PrintStream,int) print(ps,indexFlags)}.
	 *
	 * @param ps stream used for printing
	 * @see QuickHull3D#print(PrintStream,int)
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces()
	 */
	public void print (PrintStream ps)
	 {
	   print (ps, 0);
	 }

	/**
	 * Prints the vertices and faces of this hull to the stream ps.
	 *
	 * <p> This is done using the Alias Wavefront .obj file format, with
	 * the vertices printed first (each preceding by the letter
	 * <code>v</code>), followed by the vertex indices for each face (each
	 * preceded by the letter <code>f</code>).
	 *
	 * <p>By default, the face indices are numbered with respect to the
	 * hull vertices (as opposed to the input points), with a lowest index
	 * of 1, and are arranged counter-clockwise. However, this
	 * can be changed by setting {@link #POINT_RELATIVE POINT_RELATIVE},
	 * {@link #INDEXED_FROM_ONE INDEXED_FROM_ZERO}, or {@link #CLOCKWISE
	 * CLOCKWISE} in the indexFlags parameter.
	 *
	 * @param ps stream used for printing
	 * @param indexFlags specifies index characteristics
	 * (0 results in the default).
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces()
	 */
	public void print (PrintStream ps, int indexFlags)
	 {
	   if ((indexFlags & INDEXED_FROM_ZERO) == 0)
	    { indexFlags |= INDEXED_FROM_ONE;
	    }
	   for (int i=0; i<numVertices; i++)
	    { Point3d pnt = pointBuffer[vertexPointIndices[i]].pnt;
	      ps.println ("v " + pnt.x + " " + pnt.y + " " + pnt.z);
	    }
	   for (Iterator fi=faces.iterator(); fi.hasNext(); )
	    { Face face = (Face)fi.next();
	      int[] indices = new int[face.numVertices()];
	      getFaceIndices (indices, face, indexFlags);

	      ps.print ("f");
	      for (int k=0; k<indices.length; k++)
	       { ps.print (" " + indices[k]); 
	       }
	      ps.println ("");
	    }
	 }

	private void getFaceIndices (int[] indices, Face face, int flags)
	 { 
	   boolean ccw = ((flags & CLOCKWISE) == 0);
	   boolean indexedFromOne = ((flags & INDEXED_FROM_ONE) != 0);
	   boolean pointRelative = ((flags & POINT_RELATIVE) != 0);

	   HalfEdge hedge = face.he0;
	   int k = 0;
	   do
	    { int idx = hedge.head().index;
	      if (pointRelative)
	       { idx = vertexPointIndices[idx];
	       }
	      if (indexedFromOne)
	       { idx++;
	       }
	      indices[k++] = idx;
	      hedge = (ccw ? hedge.next : hedge.prev);
	    }
	   while (hedge != face.he0);	   
	 }

	protected void resolveUnclaimedPoints (FaceList newFaces)
	 {
	   Vertex vtxNext = unclaimed.first();
 	   for (Vertex vtx=vtxNext; vtx!=null; vtx=vtxNext)
 	    { vtxNext = vtx.next;
	      
	      double maxDist = tolerance;
	      Face maxFace = null;
	      for (Face newFace=newFaces.first(); newFace != null;
		   newFace=newFace.next)
	       { 
		 if (newFace.mark == Face.VISIBLE)
		  { double dist = newFace.distanceToPlane(vtx.pnt);
		    if (dist > maxDist)
		     { maxDist = dist;
		       maxFace = newFace;
		     }
		    if (maxDist > 1000*tolerance)
		     { break;
		     }
		  }
	       }
	      if (maxFace != null)
	       { 
		 addPointToFace (vtx, maxFace);
 		// if (debug && vtx.index == findIndex)
 		//  { System.out.println (findIndex + " CLAIMED BY " +
 		//     maxFace.getVertexString()); 
 		//  }
	       }
	      else
	       { 
                   
                  // if (debug && vtx.index == findIndex)
		 // { System.out.println (findIndex + " DISCARDED"); 
		 // } 
	       }
	    }
	 }

	protected void deleteFacePoints (Face face, Face absorbingFace)
	 {
	   Vertex faceVtxs = removeAllPointsFromFace (face);
	   if (faceVtxs != null)
	    { 
	      if (absorbingFace == null)
	       { unclaimed.addAll (faceVtxs);
	       }
	      else
	       { Vertex vtxNext = faceVtxs;
		 for (Vertex vtx=vtxNext; vtx!=null; vtx=vtxNext)
		  { vtxNext = vtx.next;
		    double dist = absorbingFace.distanceToPlane (vtx.pnt);
		    if (dist > tolerance)
		     { 
		       addPointToFace (vtx, absorbingFace);
		     }
		    else
		     { 
		       unclaimed.add (vtx);
		     }
		  }
	       }
	    }
	 }

	private static final int NONCONVEX_WRT_LARGER_FACE = 1;
	private static final int NONCONVEX = 2;

	protected double oppFaceDistance (HalfEdge he)
	 {
	   return he.face.distanceToPlane (he.opposite.face.getCentroid());
	 }

	private boolean doAdjacentMerge (Face face, int mergeType)
	 {
	   HalfEdge hedge = face.he0;

	   boolean convex = true;
	   do
	    { Face oppFace = hedge.oppositeFace();
	      boolean merge = false;
	      double dist1, dist2;

	      if (mergeType == NONCONVEX)
	       { // then merge faces if they are definitively non-convex
		 if (oppFaceDistance (hedge) > -tolerance ||
		     oppFaceDistance (hedge.opposite) > -tolerance)
		  { merge = true;
		  }
	       }
	      else // mergeType == NONCONVEX_WRT_LARGER_FACE
	       { // merge faces if they are parallel or non-convex
		 // wrt to the larger face; otherwise, just mark
		 // the face non-convex for the second pass.
		 if (face.area > oppFace.area)
		  { if ((dist1 = oppFaceDistance (hedge)) > -tolerance) 
		     { merge = true;
		     }
		    else if (oppFaceDistance (hedge.opposite) > -tolerance)
		     { convex = false;
		     }
		  }
		 else
		  { if (oppFaceDistance (hedge.opposite) > -tolerance)
		     { merge = true;
		     }
		    else if (oppFaceDistance (hedge) > -tolerance) 
		     { convex = false;
		     }
		  }
	       }

	      if (merge)
	       { if (debug)
		  { System.out.println (
		    "  merging " + face.getVertexString() + "  and  " +
		    oppFace.getVertexString());
		  }

		 int numd = face.mergeAdjacentFace (hedge, discardedFaces);
		 for (int i=0; i<numd; i++)
		  { deleteFacePoints (discardedFaces[i], face);
		  }
		 if (debug)
		  { System.out.println (
		       "  result: " + face.getVertexString());
		  }
		 return true;
	       }
	      hedge = hedge.next;
	    }
	   while (hedge != face.he0);
	   if (!convex)
	    { face.mark = Face.NON_CONVEX; 
	    }
	   return false;
	 }

	protected void calculateHorizon (
	   Point3d eyePnt, HalfEdge edge0, Face face, Vector horizon)
	 {
//	   oldFaces.add (face);
	   deleteFacePoints (face, null);
 	   face.mark = Face.DELETED;
	   if (debug)
	    { System.out.println ("  visiting face " + face.getVertexString());
	    }
	   HalfEdge edge;
	   if (edge0 == null)
	    { edge0 = face.getEdge(0);
	      edge = edge0;
              
              // System.out.println("face.GetEdge0>>"+edge.vertex.pnt.toString()+">>"+edge.tail().pnt.toString());
	    }
	   else
	    { edge = edge0.getNext();
            
            
           // System.out.println("face.GetEdge0.getnext>>"+edge.vertex.pnt.toString()+">>"+edge.tail().pnt.toString());

	    }
	   do
	    { Face oppFace = edge.oppositeFace();
	    
            
            
            if (oppFace.mark == Face.VISIBLE)
	       { 
                  //System.out.println("oppface.disttoplan>tolerance>>"+(oppFace.distanceToPlane (eyePnt) > tolerance));  
                 if (oppFace.distanceToPlane (eyePnt) > tolerance)
		  { calculateHorizon (eyePnt, edge.getOpposite(),
				      oppFace, horizon);
		  }
		 else
		  { horizon.add (edge);
		    if (debug)
		     { System.out.println ("  adding horizon edge " +
					   edge.getVertexString());
		     }
		  }
	       }
	      edge = edge.getNext();
	    }
	   while (edge != edge0);
	 }

	private HalfEdge addAdjoiningFace (
	   Vertex eyeVtx, HalfEdge he)
	 { 
	   Face face = Face.createTriangle (
	      eyeVtx, he.tail(), he.head());
 	   faces.add (face);
	   face.getEdge(-1).setOpposite(he.getOpposite());
	   return face.getEdge(0);
	 }

	protected void addNewFaces (
	   FaceList newFaces, Vertex eyeVtx, Vector horizon)
	 { 
	   newFaces.clear();

	   HalfEdge hedgeSidePrev = null;
	   HalfEdge hedgeSideBegin = null;

	   for (Iterator it=horizon.iterator(); it.hasNext(); ) 
	    { HalfEdge horizonHe = (HalfEdge)it.next();
	      HalfEdge hedgeSide = addAdjoiningFace (eyeVtx, horizonHe);
	      if (debug)
	       { System.out.println (
		    "new face: " + hedgeSide.face.getVertexString());
	       }
	      if (hedgeSidePrev != null)
	       { hedgeSide.next.setOpposite (hedgeSidePrev);		 
	       }
	      else
	       { hedgeSideBegin = hedgeSide; 
	       }
	      newFaces.add (hedgeSide.getFace());
	      hedgeSidePrev = hedgeSide;
	    }
	   hedgeSideBegin.next.setOpposite (hedgeSidePrev);
	 }

	protected Vertex nextPointToAdd()
	 {
	   if (!claimed.isEmpty())
               
	    { Face eyeFace = claimed.first().face;
	      Vertex eyeVtx = null;
	      double maxDist = 0;
	      for (Vertex vtx=eyeFace.outside;
		   vtx != null && vtx.face==eyeFace;
		   vtx = vtx.next)
	       { double dist = eyeFace.distanceToPlane(vtx.pnt);
		 if (dist > maxDist)
		  { maxDist = dist;
		    eyeVtx = vtx;
		  }
	       }
	      return eyeVtx;
	    }
	   else
	    { return null;
	    }
	 }
	
	protected void addPointToHull(Vertex eyeVtx)
	 {
	     horizon.clear();
	     unclaimed.clear();
	      
	     if (debug)
	      { System.out.println ("Adding point: " + eyeVtx.pnt.toString());
		System.out.println (
		   " which is " + eyeVtx.face.distanceToPlane(eyeVtx.pnt) +
		   " above face " + eyeVtx.face.getVertexString());
	      }
	     removePointFromFace (eyeVtx, eyeVtx.face);
	     calculateHorizon (eyeVtx.pnt, null, eyeVtx.face, horizon);
	     
/******************************************************************************/
           /*  
           System.out.println("The Horizon");
           for (Iterator it=horizon.iterator(); it.hasNext(); )
	    
            { HalfEdge edge = (HalfEdge)it.next();
            
              System.out.println(edge.vertex.pnt.toString()+" "+edge.tail().pnt.toString());
            
            
            }
            */ 
 /*****************************************************************************/
             newFaces.clear();
	     addNewFaces (newFaces, eyeVtx, horizon);
	     
	     // first merge pass ... merge faces which are non-convex
	     // as determined by the larger face
	     
	     for (Face face = newFaces.first(); face!=null; face=face.next)
	      { 
		if (face.mark == Face.VISIBLE)
		 { while (doAdjacentMerge(face, NONCONVEX_WRT_LARGER_FACE))
		      ;
		 }
	      }		 
	     // second merge pass ... merge faces which are non-convex
	     // wrt either face	     
	     for (Face face = newFaces.first(); face!=null; face=face.next)
	      { 
 		if (face.mark == Face.NON_CONVEX)
		 { face.mark = Face.VISIBLE;
		   while (doAdjacentMerge(face, NONCONVEX))
		      ;
 		 }
 	      }	
	     resolveUnclaimedPoints(newFaces);
	 }

	protected void buildHull ()
	 {
	   int cnt = 0;
	   Vertex eyeVtx;

	   computeMaxAndMin ();
	   createInitialSimplex ();
	   while ((eyeVtx = nextPointToAdd()) != null)
	    { addPointToHull (eyeVtx);
	      cnt++;

/******************************************************************************/	      
         int k=0;
  
          for (int i=0;i<faces.size();i++){
          faces.get(i).Id=i;
          }
              
/******************************************************************************/              
              
              if (debug)
	       { System.out.println ("iteration " + cnt + " done"); 
	       }
	    
            
            }
	   reindexFacesAndVertices();
	   if (debug)
	    { System.out.println ("hull done");
	    }
           
           
        System.out.println("-------------------------------------------------");
        
        System.out.println("The Faces Of The Hull");
        int k=0;
        for (Iterator it=faces.iterator(); it.hasNext(); )
	    { Face face = (Face)it.next();
          String S= face.he0.vertex.pnt.toString() +" - ";
          S+= face.he0.next.vertex.pnt.toString()+" - ";
          S+=face.he0.next.next.vertex.pnt.toString();
         
          System.out.println("Face>>"+k+">>>> "+S);
          k++;
        }  
        System.out.println("-------------------------------------------------");
        
           
           
	 }

	private void markFaceVertices (Face face, int mark)
	 {
	   HalfEdge he0 = face.getFirstEdge();
	   HalfEdge he = he0;
	   do
	    { he.head().index = mark;
	      he = he.next;
	    }
	   while (he != he0);
	 }

	protected void reindexFacesAndVertices()
	 { 
	   for (int i=0; i<numPoints; i++)
	    { pointBuffer[i].index = -1; 
	    }
	   // remove inactive faces and mark active vertices
	   numFaces = 0;
	   for (Iterator it=faces.iterator(); it.hasNext(); )
	    { Face face = (Face)it.next();
	      if (face.mark != Face.VISIBLE)
	       { it.remove();
	       }
	      else
	       { markFaceVertices (face, 0);
		 numFaces++;
	       }
	    }
	   // reindex vertices
	   numVertices = 0;
	   for (int i=0; i<numPoints; i++)
	    { Vertex vtx = pointBuffer[i];
	      if (vtx.index == 0)
	       { vertexPointIndices[numVertices] = i;
		 vtx.index = numVertices++;
	       }
	    }
	 }

	protected boolean checkFaceConvexity (
	   Face face, double tol, PrintStream ps)
	 {
	   double dist;
	   HalfEdge he = face.he0;
	   do
	    { face.checkConsistency();
	      // make sure edge is convex
	      dist = oppFaceDistance (he);
	      if (dist > tol)
	       { if (ps != null)
		  { ps.println ("Edge " + he.getVertexString() +
				" non-convex by " + dist);
		  }
		 return false;
	       }
	      dist = oppFaceDistance (he.opposite);
	      if (dist > tol)
	       { if (ps != null)
		  { ps.println ("Opposite edge " +
				he.opposite.getVertexString() +
				" non-convex by " + dist);
		  }
		 return false;
	       }
	      if (he.next.oppositeFace() == he.oppositeFace())
	       { if (ps != null)
		  { ps.println ("Redundant vertex " + he.head().index +
				" in face " + face.getVertexString());
		  }
		 return false;
	       }
	      he = he.next;
	    }
	   while (he != face.he0);	   
	   return true;
	 }

	protected boolean checkFaces(double tol, PrintStream ps)
	 { 
	   // check edge convexity
	   boolean convex = true;
	   for (Iterator it=faces.iterator(); it.hasNext(); ) 
	    { Face face = (Face)it.next();
	      if (face.mark == Face.VISIBLE)
	       { if (!checkFaceConvexity (face, tol, ps))
		  { convex = false;
		  }
	       }
	    }
	   return convex;
	 }

	/**
	 * Checks the correctness of the hull using the distance tolerance
	 * returned by {@link QuickHull3D#getDistanceTolerance
	 * getDistanceTolerance}; see
	 * {@link QuickHull3D#check(PrintStream,double)
	 * check(PrintStream,double)} for details.
	 *
	 * @param ps print stream for diagnostic messages; may be
	 * set to <code>null</code> if no messages are desired.
	 * @return true if the hull is valid
	 * @see QuickHull3D#check(PrintStream,double)
	 */
	public boolean check (PrintStream ps)
	 {
	   return check (ps, getDistanceTolerance());
	 }

	/**
	 * Checks the correctness of the hull. This is done by making sure that
	 * no faces are non-convex and that no points are outside any face.
	 * These tests are performed using the distance tolerance <i>tol</i>.
	 * Faces are considered non-convex if any edge is non-convex, and an
	 * edge is non-convex if the centroid of either adjoining face is more
	 * than <i>tol</i> above the plane of the other face. Similarly,
	 * a point is considered outside a face if its distance to that face's
	 * plane is more than 10 times <i>tol</i>.
	 *
	 * <p>If the hull has been {@link #triangulate triangulated},
	 * then this routine may fail if some of the resulting
	 * triangles are very small or thin.
	 *
	 * @param ps print stream for diagnostic messages; may be
	 * set to <code>null</code> if no messages are desired.
	 * @param tol distance tolerance
	 * @return true if the hull is valid
	 * @see QuickHull3D#check(PrintStream)
	 */
	public boolean check (PrintStream ps, double tol)

	 {
	   // check to make sure all edges are fully connected
	   // and that the edges are convex
	   double dist;
	   double pointTol = 10*tol;

	   if (!checkFaces(tolerance, ps))
	    { return false; 
	    }

	   // check point inclusion

	   for (int i=0; i<numPoints; i++)
	    { Point3d pnt = pointBuffer[i].pnt;
	      for (Iterator it=faces.iterator(); it.hasNext(); ) 
	       { Face face = (Face)it.next();
		 if (face.mark == Face.VISIBLE)
		  { 
		    dist = face.distanceToPlane (pnt);
		    if (dist > pointTol)
		     { if (ps != null)
			{ ps.println (
			     "Point " + i + " " + dist + " above face " +
			     face.getVertexString());
			}
		       return false;
		     }
		  }
	       }
	    }
	   return true;
	 }

     
     
     
  /****************************************************************************/  
     
    private Text TKey = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
       
        
   /***************************************************************************/
        
        ArrayList<Point3d> Points = new ArrayList<Point3d>();
        
        String line = value.toString();
        
        if (line.length()>1){
        
        String []linesArr = line.split("\n");
        
        for (int i=0; i<linesArr.length;i++){
        
            String[] S=linesArr[i].split(" ");
         
           
            
            Point3d TVertex=new Point3d();
            
           
            TVertex.x=Double.valueOf(S[0]);
            TVertex.y=Double.valueOf(S[1]);
            TVertex.z=Double.valueOf(S[2]);
            Points.add(TVertex);
                
                
          
            
            }
        }
       
/******************************************************************************/
        
        
        
        if (Points.size()>=5){
   
        try { 
        
        Point3d[] PointsArray=new Point3d[Points.size()];
        build(Points.toArray(PointsArray));
        
        Point3d[] vertices=getVertices();
        for (int i=0;i<vertices.length;i++){
        // context.write(new Text("Reducer1"),new Text(vertices[i].toString()));
        
         String TKey=vertices[i].toString().replace(" ","");
         context.write(new Text(TKey),new Text(vertices[i].toString()));    
        }
        }
        catch (Exception e) {
        
            for (int i=0;i<Points.size();i++){
            
                
                String TKey=Points.get(i).toString().replace(" ","");
                context.write(new Text(TKey),new Text(Points.get(i).toString())); 
                
            }
          
            
            
            
            
        }    
            
            
        }
        
        
        
        else {
         
            for (int i=0;i<Points.size();i++){
            
                
                String TKey=Points.get(i).toString().replace(" ","");
                context.write(new Text(TKey),new Text(Points.get(i).toString())); 
                
            }
          
              
        }
        
       
     
       
      
    }

 }
 public static class QHReducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   

    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      
        
        
        for (Text val:values){
        
        context.write(null, val);
        }
        
        
        
        
    }
  }
         
 
 
 
/*****************************************************************************/

/*****************************************************************************/
 
        
 public static class FinalQHMapper 
       extends Mapper<Object, Text, Text, Text>{
     
    private Text TKey = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        
        
        if (value.toString().isEmpty()==false){
        context.write(new Text ("Reducer1"), value);}
        
        
        }
        
       
     
       
      
    }

 
 public static class FinalQHReducer 
       extends Reducer<Text,Text,NullWritable,Text> {
   
    
   //***************************************************************************
     	/**
	 * Specifies that (on output) vertex indices for a face should be
	 * listed in clockwise order.
	 */
     public static final int CLOCKWISE = 0x1;

	/**
	 * Specifies that (on output) the vertex indices for a face should be
	 * numbered starting from 1.
	 */
      public static final int INDEXED_FROM_ONE = 0x2;

	/**
	 * Specifies that (on output) the vertex indices for a face should be
	 * numbered starting from 0.
	 */
      public static final int INDEXED_FROM_ZERO = 0x4;

	/**
	 * Specifies that (on output) the vertex indices for a face should be
	 * numbered with respect to the original input points.
	 */
       public static final int POINT_RELATIVE = 0x8;

	/**
	 * Specifies that the distance tolerance should be
	 * computed automatically from the input point data.
	 */
	public static final double AUTOMATIC_TOLERANCE = -1;

	protected int findIndex = -1;

	// estimated size of the point set
	protected double charLength;

	protected boolean debug = false;

	protected Vertex[] pointBuffer = new Vertex[0];
	protected int[] vertexPointIndices = new int[0];
	private Face[] discardedFaces = new Face[3];

	private Vertex[] maxVtxs = new Vertex[3];
	private Vertex[] minVtxs = new Vertex[3];

	protected ArrayList<Face> faces = new ArrayList<Face>(16);
	protected Vector horizon = new Vector(16);

	private FaceList newFaces = new FaceList();
	private VertexList unclaimed = new VertexList();
	private VertexList claimed = new VertexList();
        ArrayList<Face>TempC=new ArrayList<Face>();
        
	protected int numVertices;
	protected int numFaces;
	protected int numPoints;

	protected double explicitTolerance = AUTOMATIC_TOLERANCE;
	protected double tolerance;

	/**
	 * Returns true if debugging is enabled.
	 *
	 * @return true is debugging is enabled
	 * @see QuickHull3D#setDebug
	 */
	public boolean getDebug()
	 {
	   return debug;
	 }

	/**
	 * Enables the printing of debugging diagnostics.
	 *
	 * @param enable if true, enables debugging
	 */
	public void setDebug (boolean enable)
	 { 
	   debug = enable;
	 }

	/**
	 * Precision of a double.
	 */
	static private final double DOUBLE_PREC = 2.2204460492503131e-16;


	/**
	 * Returns the distance tolerance that was used for the most recently
	 * computed hull. The distance tolerance is used to determine when
	 * faces are unambiguously convex with respect to each other, and when
	 * points are unambiguously above or below a face plane, in the
	 * presence of <a href=#distTol>numerical imprecision</a>. Normally,
	 * this tolerance is computed automatically for each set of input
	 * points, but it can be set explicitly by the application.
	 *
	 * @return distance tolerance
	 * @see QuickHull3D#setExplicitDistanceTolerance
	 */
	public double getDistanceTolerance()
	 {
	   return tolerance;
	 }

	/**
	 * Sets an explicit distance tolerance for convexity tests.
	 * If {@link #AUTOMATIC_TOLERANCE AUTOMATIC_TOLERANCE}
	 * is specified (the default), then the tolerance will be computed
	 * automatically from the point data.
	 *
	 * @param tol explicit tolerance
	 * @see #getDistanceTolerance
	 */
	public void setExplicitDistanceTolerance(double tol)
	 { 
	   explicitTolerance = tol;
	 }

	/**
	 * Returns the explicit distance tolerance.
	 *
	 * @return explicit tolerance
	 * @see #setExplicitDistanceTolerance
	 */
	public double getExplicitDistanceTolerance()
	 {
	   return explicitTolerance;
	 }

	private void addPointToFace (Vertex vtx, Face face)
	 {
	   vtx.face = face;

	   if (face.outside == null)
	    { claimed.add (vtx);
	    }
	   else
	    { claimed.insertBefore (vtx, face.outside); 
	    }
	   face.outside = vtx;
	 }

	private void removePointFromFace (Vertex vtx, Face face)
	 {
	   if (vtx == face.outside)
	    { if (vtx.next != null && vtx.next.face == face)
	       { face.outside = vtx.next;
	       }
	      else
	       { face.outside = null; 
	       }
	    }
	   claimed.delete (vtx);
	 }

	private Vertex removeAllPointsFromFace (Face face)
	 {
	   if (face.outside != null)
	    { 
	      Vertex end = face.outside;
	      while (end.next != null && end.next.face == face)
	       { end = end.next;
	       }
	      claimed.delete (face.outside, end);
	      end.next = null;
	      return face.outside;
	    }
	   else
	    { return null; 
	    }
	 }

	

	
	private HalfEdge findHalfEdge (Vertex tail, Vertex head)
	 { 
	   // brute force ... OK, since setHull is not used much
	   for (Iterator it=faces.iterator(); it.hasNext(); ) 
	    { HalfEdge he = ((Face)it.next()).findEdge (tail, head);
	      if (he != null)
	       { return he; 
	       }
	    }
	   return null;
	 }

 	protected void setHull (double[] coords, int nump,
				int[][] faceIndices, int numf)
 	 {
 	   initBuffers (nump);
	   setPoints (coords, nump);
	   computeMaxAndMin ();
	   for (int i=0; i<numf; i++)
	    { Face face = Face.create (pointBuffer, faceIndices[i]);
	      HalfEdge he = face.he0;
	      do
	       { HalfEdge heOpp = findHalfEdge (he.head(), he.tail());
		 if (heOpp != null)
		  { he.setOpposite (heOpp); 
		  }
		 he = he.next;
	       }
	      while (he != face.he0);
	      faces.add (face);
	    }
 	 }

	private void printQhullErrors (Process proc)
	   throws IOException
	 {
	   boolean wrote = false;
	   InputStream es = proc.getErrorStream();
	   while (es.available() > 0)
	    { System.out.write (es.read());
	      wrote = true;
	    }
	   if (wrote)
	    { System.out.println("");
	    }
	 }

	protected void setFromQhull (double[] coords, int nump,
				     boolean triangulate)
	 {
	   String commandStr = "./qhull i";
	   if (triangulate)
	    { commandStr += " -Qt"; 
	    }
	   try
	    { 
	      Process proc = Runtime.getRuntime().exec (commandStr);
	      PrintStream ps = new PrintStream (proc.getOutputStream());
	      StreamTokenizer stok =
		 new StreamTokenizer (
		    new InputStreamReader (proc.getInputStream()));

	      ps.println ("3 " + nump);
	      for (int i=0; i<nump; i++)
	       { ps.println (
		    coords[i*3+0] + " " +
		    coords[i*3+1] + " " +  
		    coords[i*3+2]);
	       }
	      ps.flush();
	      ps.close();
	      Vector indexList = new Vector(3);
	      stok.eolIsSignificant(true);
	      printQhullErrors (proc);
	      
	      do
	       { stok.nextToken();
	       }
	      while (stok.sval == null ||
		     !stok.sval.startsWith ("MERGEexact"));
	      for (int i=0; i<4; i++)
	       { stok.nextToken();
	       }
	      if (stok.ttype != StreamTokenizer.TT_NUMBER)
	       { System.out.println ("Expecting number of faces");
		 System.exit(1); 
	       }
	      int numf = (int)stok.nval;
	      stok.nextToken(); // clear EOL
	      int[][] faceIndices = new int[numf][];
	      for (int i=0; i<numf; i++)
	       { indexList.clear();
		 while (stok.nextToken() != StreamTokenizer.TT_EOL)
		  { if (stok.ttype != StreamTokenizer.TT_NUMBER)
		     { System.out.println ("Expecting face index");
		       System.exit(1); 
		     }
		    indexList.add (0, new Integer((int)stok.nval));
		  }
		 faceIndices[i] = new int[indexList.size()];
		 int k = 0;
		 for (Iterator it=indexList.iterator(); it.hasNext(); ) 
		  { faceIndices[i][k++] = ((Integer)it.next()).intValue();
		  }
	       }
	      setHull (coords, nump, faceIndices, numf);
	    }
	   catch (Exception e) 
	    { e.printStackTrace();
	      System.exit(1); 
	    }
	 }

	private void printPoints (PrintStream ps)
	 {
	   for (int i=0; i<numPoints; i++)
	    { Point3d pnt = pointBuffer[i].pnt;
	      ps.println (pnt.x + ", " + pnt.y + ", " + pnt.z + ",");
	    }
	 }

	/**
	 * Constructs the convex hull of a set of points whose
	 * coordinates are given by an array of doubles.
	 *
	 * @param coords x, y, and z coordinates of each input
	 * point. The length of this array will be three times
	 * the number of input points.
	 * @throws IllegalArgumentException the number of input points is less
	 * than four, or the points appear to be coincident, colinear, or
	 * coplanar.
	 */
	public void build (double[] coords)
	   throws IllegalArgumentException
	 {
	   build (coords, coords.length/3);
	 }

	/**
	 * Constructs the convex hull of a set of points whose
	 * coordinates are given by an array of doubles.
	 *
	 * @param coords x, y, and z coordinates of each input
	 * point. The length of this array must be at least three times
	 * <code>nump</code>.
	 * @param nump number of input points
	 * @throws IllegalArgumentException the number of input points is less
	 * than four or greater than 1/3 the length of <code>coords</code>,
	 * or the points appear to be coincident, colinear, or
	 * coplanar.
	 */
	public void build (double[] coords, int nump)
	   throws IllegalArgumentException
	 {
	   if (nump < 4)
	    { throw new IllegalArgumentException (
		 "Less than four input points specified");
	    }
	   if (coords.length/3 < nump)
	    { throw new IllegalArgumentException (
		 "Coordinate array too small for specified number of points"); 
	    }
	   initBuffers (nump);
	   setPoints (coords, nump);
	   buildHull ();
	 }

	/**
	 * Constructs the convex hull of a set of points.
	 *
	 * @param points input points
	 * @throws IllegalArgumentException the number of input points is less
	 * than four, or the points appear to be coincident, colinear, or
	 * coplanar.
	 */
	public void build (Point3d[] points)
	   throws IllegalArgumentException
	 {
	   build (points, points.length);
	 }

	/**
	 * Constructs the convex hull of a set of points.
	 *
	 * @param points input points
	 * @param nump number of input points
	 * @throws IllegalArgumentException the number of input points is less
	 * than four or greater then the length of <code>points</code>, or the
	 * points appear to be coincident, colinear, or coplanar.
	 */
	public void build (Point3d[] points, int nump)
	   throws IllegalArgumentException
	 {
	   if (nump < 4)
	    { throw new IllegalArgumentException (
		 "Less than four input points specified");
	    }
	   if (points.length < nump)
	    { throw new IllegalArgumentException (
		 "Point array too small for specified number of points"); 
	    }
	   initBuffers (nump);
	   setPoints (points, nump);
	   buildHull ();
	 }

	/**
	 * Triangulates any non-triangular hull faces. In some cases, due to
	 * precision issues, the resulting triangles may be very thin or small,
	 * and hence appear to be non-convex (this same limitation is present
	 * in <a href=http://www.qhull.org>qhull</a>).
	 */
	public void triangulate ()
	 {
	   double minArea = 1000*charLength*DOUBLE_PREC;
	   newFaces.clear();
	   for (Iterator it=faces.iterator(); it.hasNext(); ) 
	    { Face face = (Face)it.next();
	      if (face.mark == Face.VISIBLE)
	       { 
		 face.triangulate (newFaces, minArea);
		 // splitFace (face);
	       }
	    }
	   for (Face face=newFaces.first(); face!=null; face=face.next)
	    { faces.add (face);
	    }
	 }

	protected void initBuffers (int nump)
	 {
	   if (pointBuffer.length < nump)
	    { Vertex[] newBuffer = new Vertex[nump];
	      vertexPointIndices = new int[nump];
	      for (int i=0; i<pointBuffer.length; i++)
	       { newBuffer[i] = pointBuffer[i]; 
	       }
	      for (int i=pointBuffer.length; i<nump; i++)
	       { newBuffer[i] = new Vertex(); 
	       }
	      pointBuffer = newBuffer;
	    }
	   faces.clear();
	   claimed.clear();
	   numFaces = 0;
	   numPoints = nump;
	 }

	protected void setPoints (double[] coords, int nump)
	 { 
	   for (int i=0; i<nump; i++)
	    { 
	      Vertex vtx = pointBuffer[i];
	      vtx.pnt.set (coords[i*3+0], coords[i*3+1], coords[i*3+2]);
	      vtx.index = i;
	    }
	 }

	protected void setPoints (Point3d[] pnts, int nump)
	 { 
	   for (int i=0; i<nump; i++)
	    { 
	      Vertex vtx = pointBuffer[i];
	      vtx.pnt.set (pnts[i]);
	      vtx.index = i;
	    }
	 }

	protected void computeMaxAndMin ()
	 {
	   Vector3d max = new Vector3d();
	   Vector3d min = new Vector3d();

	   for (int i=0; i<3; i++)
	    { maxVtxs[i] = minVtxs[i] = pointBuffer[0]; 
	    }
	   max.set (pointBuffer[0].pnt);
	   min.set (pointBuffer[0].pnt);

	   for (int i=1; i<numPoints; i++)
	    { Point3d pnt = pointBuffer[i].pnt;
	      if (pnt.x > max.x)
	       { max.x = pnt.x;
		 maxVtxs[0] = pointBuffer[i];
	       }
	      else if (pnt.x < min.x)
	       { min.x = pnt.x;
		 minVtxs[0] = pointBuffer[i];
	       }
	      if (pnt.y > max.y)
	       { max.y = pnt.y;
		 maxVtxs[1] = pointBuffer[i];
	       }
	      else if (pnt.y < min.y)
	       { min.y = pnt.y;
		 minVtxs[1] = pointBuffer[i];
	       }
	      if (pnt.z > max.z)
	       { max.z = pnt.z;
		 maxVtxs[2] = pointBuffer[i];
	       }
	      else if (pnt.z < min.z)
	       { min.z = pnt.z;
		 minVtxs[2] = pointBuffer[i];
	       }
	    }

	   // this epsilon formula comes from QuickHull, and I'm
	   // not about to quibble.
	   // We use the charLength at the triangulate function
           charLength = Math.max(max.x-min.x, max.y-min.y);
	   charLength = Math.max(max.z-min.z, charLength);
	   if (explicitTolerance == AUTOMATIC_TOLERANCE)
	    { tolerance =
		 3*DOUBLE_PREC*(Math.max(Math.abs(max.x),Math.abs(min.x))+
				Math.max(Math.abs(max.y),Math.abs(min.y))+
				Math.max(Math.abs(max.z),Math.abs(min.z)));
	    }
	   else
	    { tolerance = explicitTolerance; 
	    }
	 }

	/**
	 * Creates the initial simplex from which the hull will be built.
	 */
	protected void createInitialSimplex ()
	   throws IllegalArgumentException
	 {
	   double max = 0;
	   int imax = 0;

	   for (int i=0; i<3; i++)
	    { double diff = maxVtxs[i].pnt.get(i)-minVtxs[i].pnt.get(i);
	      if (diff > max)
	       { max = diff;
		 imax = i;
	       }
 	    }

	   if (max <= tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be coincident");
	    }
	   Vertex[] vtx = new Vertex[4];
	   // set first two vertices to be those with the greatest
	   // one dimensional separation

	   vtx[0] = maxVtxs[imax];
	   vtx[1] = minVtxs[imax];

	   // set third vertex to be the vertex farthest from
	   // the line between vtx0 and vtx1
	   Vector3d u01 = new Vector3d();
	   Vector3d diff02 = new Vector3d();
	   Vector3d nrml = new Vector3d();
	   Vector3d xprod = new Vector3d();
	   double maxSqr = 0;
	   u01.sub (vtx[1].pnt, vtx[0].pnt);
	   u01.normalize();
	   for (int i=0; i<numPoints; i++)
	    { diff02.sub (pointBuffer[i].pnt, vtx[0].pnt);
	      xprod.cross (u01, diff02);
	      double lenSqr = xprod.normSquared();
	      if (lenSqr > maxSqr &&
		  pointBuffer[i] != vtx[0] &&  // paranoid
		  pointBuffer[i] != vtx[1])
	       { maxSqr = lenSqr; 
		 vtx[2] = pointBuffer[i];
		 nrml.set (xprod);
	       }
	    }
	   if (Math.sqrt(maxSqr) <= 100*tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be colinear");
	    }
	   nrml.normalize();


	   double maxDist = 0;
	   double d0 = vtx[2].pnt.dot (nrml);
	   for (int i=0; i<numPoints; i++)
	    { double dist = Math.abs (pointBuffer[i].pnt.dot(nrml) - d0);
	      if (dist > maxDist &&
		  pointBuffer[i] != vtx[0] &&  // paranoid
		  pointBuffer[i] != vtx[1] &&
		  pointBuffer[i] != vtx[2])
	       { maxDist = dist;
		 vtx[3] = pointBuffer[i];
	       }
	    }
	   if (Math.abs(maxDist) <= 100*tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be coplanar"); 
	    }

	   if (debug)
	    { System.out.println ("initial vertices:");
	      System.out.println (vtx[0].index + ": " + vtx[0].pnt);
	      System.out.println (vtx[1].index + ": " + vtx[1].pnt);
	      System.out.println (vtx[2].index + ": " + vtx[2].pnt);
	      System.out.println (vtx[3].index + ": " + vtx[3].pnt);
	    }

	   Face[] tris = new Face[4];

	   if (vtx[3].pnt.dot (nrml) - d0 < 0)
	    { tris[0] = Face.createTriangle (vtx[0], vtx[1], vtx[2]);
	      tris[1] = Face.createTriangle (vtx[3], vtx[1], vtx[0]);
	      tris[2] = Face.createTriangle (vtx[3], vtx[2], vtx[1]);
	      tris[3] = Face.createTriangle (vtx[3], vtx[0], vtx[2]);

	      for (int i=0; i<3; i++)
	       { int k = (i+1)%3;
		 tris[i+1].getEdge(1).setOpposite (tris[k+1].getEdge(0));
		 tris[i+1].getEdge(2).setOpposite (tris[0].getEdge(k));
	       }
	    }
	   else
	    { tris[0] = Face.createTriangle (vtx[0], vtx[2], vtx[1]);
	      tris[1] = Face.createTriangle (vtx[3], vtx[0], vtx[1]);
	      tris[2] = Face.createTriangle (vtx[3], vtx[1], vtx[2]);
	      tris[3] = Face.createTriangle (vtx[3], vtx[2], vtx[0]);

	      for (int i=0; i<3; i++)
	       { int k = (i+1)%3;
		 tris[i+1].getEdge(0).setOpposite (tris[k+1].getEdge(1));
		 tris[i+1].getEdge(2).setOpposite (tris[0].getEdge((3-i)%3));
	       }
	    }


 	   for (int i=0; i<4; i++)
 	    { faces.add (tris[i]); 
 	    }

	   for (int i=0; i<numPoints; i++)
	    { Vertex v = pointBuffer[i];

	      if (v == vtx[0] || v == vtx[1] || v == vtx[2] || v == vtx[3])
	       { continue;
	       }

	      maxDist = tolerance;
	      Face maxFace = null;
	      for (int k=0; k<4; k++)
	       { double dist = tris[k].distanceToPlane (v.pnt);
		 if (dist > maxDist)
		  { maxFace = tris[k];
		    maxDist = dist;
		  }
	       }
	      if (maxFace != null)
	       { addPointToFace (v, maxFace);
	       }	      
	    }
	 }

	/**
	 * Returns the number of vertices in this hull.
	 *
	 * @return number of vertices
	 */
	public int getNumVertices()
	 {
	   return numVertices;
	 }

	/**
	 * Returns the vertex points in this hull.
	 *
	 * @return array of vertex points
	 * @see QuickHull3D#getVertices(double[])
	 * @see QuickHull3D#getFaces()
	 */
 	public Point3d[] getVertices()
 	 {
 	   Point3d[] vtxs = new Point3d[numVertices];
 	   for (int i=0; i<numVertices; i++)
	    { vtxs[i] = pointBuffer[vertexPointIndices[i]].pnt;
	    }
	   return vtxs;
	 }

	/**
	 * Returns the coordinates of the vertex points of this hull.
	 *
	 * @param coords returns the x, y, z coordinates of each vertex.
	 * This length of this array must be at least three times
	 * the number of vertices.
	 * @return the number of vertices
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces()
	 */
 	public int getVertices(double[] coords)
 	 {
 	   for (int i=0; i<numVertices; i++)
	    { Point3d pnt = pointBuffer[vertexPointIndices[i]].pnt;
	      coords[i*3+0] = pnt.x;
	      coords[i*3+1] = pnt.y;
	      coords[i*3+2] = pnt.z;
	    }
	   return numVertices;
	 }

	/**
	 * Returns an array specifing the index of each hull vertex
	 * with respect to the original input points.
	 *
	 * @return vertex indices with respect to the original points
	 */
	public int[] getVertexPointIndices()
	 { 
	   int[] indices = new int[numVertices];
	   for (int i=0; i<numVertices; i++)
	    { indices[i] = vertexPointIndices[i];
	    }
	   return indices;
	 }

	/**
	 * Returns the number of faces in this hull.
	 *
	 * @return number of faces
	 */
	public int getNumFaces()
	 { 
	   return faces.size();
	 }

	/**
	 * Returns the faces associated with this hull.
	 *
	 * <p>Each face is represented by an integer array which gives the
	 * indices of the vertices. These indices are numbered
	 * relative to the
	 * hull vertices, are zero-based,
	 * and are arranged counter-clockwise. More control
	 * over the index format can be obtained using
	 * {@link #getFaces(int) getFaces(indexFlags)}.
	 *
	 * @return array of integer arrays, giving the vertex
	 * indices for each face.
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces(int)
	 */
	public int[][] getFaces ()
	 {
	   return getFaces(0);
	 }

	/**
	 * Returns the faces associated with this hull.
	 *
	 * <p>Each face is represented by an integer array which gives the
	 * indices of the vertices. By default, these indices are numbered with
	 * respect to the hull vertices (as opposed to the input points), are
	 * zero-based, and are arranged counter-clockwise. However, this
	 * can be changed by setting {@link #POINT_RELATIVE
	 * POINT_RELATIVE}, {@link #INDEXED_FROM_ONE INDEXED_FROM_ONE}, or
	 * {@link #CLOCKWISE CLOCKWISE} in the indexFlags parameter.
	 *
	 * @param indexFlags specifies index characteristics (0 results
	 * in the default)
	 * @return array of integer arrays, giving the vertex
	 * indices for each face.
	 * @see QuickHull3D#getVertices()
	 */
	public int[][] getFaces (int indexFlags)
	 {
	   int[][] allFaces = new int[faces.size()][];
	   int k = 0;
	   for (Iterator it=faces.iterator(); it.hasNext(); )
	    { Face face = (Face)it.next();
	      allFaces[k] = new int[face.numVertices()];
	      getFaceIndices (allFaces[k], face, indexFlags);
	      k++;
	    }
	   return allFaces;
	 }

	/**
	 * Prints the vertices and faces of this hull to the stream ps.
	 *
	 * <p>
	 * This is done using the Alias Wavefront .obj file
	 * format, with the vertices printed first (each preceding by
	 * the letter <code>v</code>), followed by the vertex indices
	 * for each face (each
	 * preceded by the letter <code>f</code>).
	 *
	 * <p>The face indices are numbered with respect to the hull vertices
	 * (as opposed to the input points), with a lowest index of 1, and are
	 * arranged counter-clockwise. More control over the index format can
	 * be obtained using
	 * {@link #print(PrintStream,int) print(ps,indexFlags)}.
	 *
	 * @param ps stream used for printing
	 * @see QuickHull3D#print(PrintStream,int)
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces()
	 */
	public void print (PrintStream ps)
	 {
	   print (ps, 0);
	 }

	/**
	 * Prints the vertices and faces of this hull to the stream ps.
	 *
	 * <p> This is done using the Alias Wavefront .obj file format, with
	 * the vertices printed first (each preceding by the letter
	 * <code>v</code>), followed by the vertex indices for each face (each
	 * preceded by the letter <code>f</code>).
	 *
	 * <p>By default, the face indices are numbered with respect to the
	 * hull vertices (as opposed to the input points), with a lowest index
	 * of 1, and are arranged counter-clockwise. However, this
	 * can be changed by setting {@link #POINT_RELATIVE POINT_RELATIVE},
	 * {@link #INDEXED_FROM_ONE INDEXED_FROM_ZERO}, or {@link #CLOCKWISE
	 * CLOCKWISE} in the indexFlags parameter.
	 *
	 * @param ps stream used for printing
	 * @param indexFlags specifies index characteristics
	 * (0 results in the default).
	 * @see QuickHull3D#getVertices()
	 * @see QuickHull3D#getFaces()
	 */
	public void print (PrintStream ps, int indexFlags)
	 {
	   if ((indexFlags & INDEXED_FROM_ZERO) == 0)
	    { indexFlags |= INDEXED_FROM_ONE;
	    }
	   for (int i=0; i<numVertices; i++)
	    { Point3d pnt = pointBuffer[vertexPointIndices[i]].pnt;
	      ps.println ("v " + pnt.x + " " + pnt.y + " " + pnt.z);
	    }
	   for (Iterator fi=faces.iterator(); fi.hasNext(); )
	    { Face face = (Face)fi.next();
	      int[] indices = new int[face.numVertices()];
	      getFaceIndices (indices, face, indexFlags);

	      ps.print ("f");
	      for (int k=0; k<indices.length; k++)
	       { ps.print (" " + indices[k]); 
	       }
	      ps.println ("");
	    }
	 }

	private void getFaceIndices (int[] indices, Face face, int flags)
	 { 
	   boolean ccw = ((flags & CLOCKWISE) == 0);
	   boolean indexedFromOne = ((flags & INDEXED_FROM_ONE) != 0);
	   boolean pointRelative = ((flags & POINT_RELATIVE) != 0);

	   HalfEdge hedge = face.he0;
	   int k = 0;
	   do
	    { int idx = hedge.head().index;
	      if (pointRelative)
	       { idx = vertexPointIndices[idx];
	       }
	      if (indexedFromOne)
	       { idx++;
	       }
	      indices[k++] = idx;
	      hedge = (ccw ? hedge.next : hedge.prev);
	    }
	   while (hedge != face.he0);	   
	 }

	protected void resolveUnclaimedPoints (FaceList newFaces)
	 {
	   Vertex vtxNext = unclaimed.first();
 	   for (Vertex vtx=vtxNext; vtx!=null; vtx=vtxNext)
 	    { vtxNext = vtx.next;
	      
	      double maxDist = tolerance;
	      Face maxFace = null;
	      for (Face newFace=newFaces.first(); newFace != null;
		   newFace=newFace.next)
	       { 
		 if (newFace.mark == Face.VISIBLE)
		  { double dist = newFace.distanceToPlane(vtx.pnt);
		    if (dist > maxDist)
		     { maxDist = dist;
		       maxFace = newFace;
		     }
		    if (maxDist > 1000*tolerance)
		     { break;
		     }
		  }
	       }
	      if (maxFace != null)
	       { 
		 addPointToFace (vtx, maxFace);
 		// if (debug && vtx.index == findIndex)
 		//  { System.out.println (findIndex + " CLAIMED BY " +
 		//     maxFace.getVertexString()); 
 		//  }
	       }
	      else
	       { 
                   
                  // if (debug && vtx.index == findIndex)
		 // { System.out.println (findIndex + " DISCARDED"); 
		 // } 
	       }
	    }
	 }

	protected void deleteFacePoints (Face face, Face absorbingFace)
	 {
	   Vertex faceVtxs = removeAllPointsFromFace (face);
	   if (faceVtxs != null)
	    { 
	      if (absorbingFace == null)
	       { unclaimed.addAll (faceVtxs);
	       }
	      else
	       { Vertex vtxNext = faceVtxs;
		 for (Vertex vtx=vtxNext; vtx!=null; vtx=vtxNext)
		  { vtxNext = vtx.next;
		    double dist = absorbingFace.distanceToPlane (vtx.pnt);
		    if (dist > tolerance)
		     { 
		       addPointToFace (vtx, absorbingFace);
		     }
		    else
		     { 
		       unclaimed.add (vtx);
		     }
		  }
	       }
	    }
	 }

	private static final int NONCONVEX_WRT_LARGER_FACE = 1;
	private static final int NONCONVEX = 2;

	protected double oppFaceDistance (HalfEdge he)
	 {
	   return he.face.distanceToPlane (he.opposite.face.getCentroid());
	 }

	private boolean doAdjacentMerge (Face face, int mergeType)
	 {
	   HalfEdge hedge = face.he0;

	   boolean convex = true;
	   do
	    { Face oppFace = hedge.oppositeFace();
	      boolean merge = false;
	      double dist1, dist2;

	      if (mergeType == NONCONVEX)
	       { // then merge faces if they are definitively non-convex
		 if (oppFaceDistance (hedge) > -tolerance ||
		     oppFaceDistance (hedge.opposite) > -tolerance)
		  { merge = true;
		  }
	       }
	      else // mergeType == NONCONVEX_WRT_LARGER_FACE
	       { // merge faces if they are parallel or non-convex
		 // wrt to the larger face; otherwise, just mark
		 // the face non-convex for the second pass.
		 if (face.area > oppFace.area)
		  { if ((dist1 = oppFaceDistance (hedge)) > -tolerance) 
		     { merge = true;
		     }
		    else if (oppFaceDistance (hedge.opposite) > -tolerance)
		     { convex = false;
		     }
		  }
		 else
		  { if (oppFaceDistance (hedge.opposite) > -tolerance)
		     { merge = true;
		     }
		    else if (oppFaceDistance (hedge) > -tolerance) 
		     { convex = false;
		     }
		  }
	       }

	      if (merge)
	       { if (debug)
		  { System.out.println (
		    "  merging " + face.getVertexString() + "  and  " +
		    oppFace.getVertexString());
		  }

		 int numd = face.mergeAdjacentFace (hedge, discardedFaces);
		 for (int i=0; i<numd; i++)
		  { deleteFacePoints (discardedFaces[i], face);
		  }
		 if (debug)
		  { System.out.println (
		       "  result: " + face.getVertexString());
		  }
		 return true;
	       }
	      hedge = hedge.next;
	    }
	   while (hedge != face.he0);
	   if (!convex)
	    { face.mark = Face.NON_CONVEX; 
	    }
	   return false;
	 }

	protected void calculateHorizon (
	   Point3d eyePnt, HalfEdge edge0, Face face, Vector horizon)
	 {
//	   oldFaces.add (face);
	   deleteFacePoints (face, null);
 	   face.mark = Face.DELETED;
	   if (debug)
	    { System.out.println ("  visiting face " + face.getVertexString());
	    }
	   HalfEdge edge;
	   if (edge0 == null)
	    { edge0 = face.getEdge(0);
	      edge = edge0;
              
              // System.out.println("face.GetEdge0>>"+edge.vertex.pnt.toString()+">>"+edge.tail().pnt.toString());
	    }
	   else
	    { edge = edge0.getNext();
            
            
           // System.out.println("face.GetEdge0.getnext>>"+edge.vertex.pnt.toString()+">>"+edge.tail().pnt.toString());

	    }
	   do
	    { Face oppFace = edge.oppositeFace();
	    
            
            
            if (oppFace.mark == Face.VISIBLE)
	       { 
                  //System.out.println("oppface.disttoplan>tolerance>>"+(oppFace.distanceToPlane (eyePnt) > tolerance));  
                 if (oppFace.distanceToPlane (eyePnt) > tolerance)
		  { calculateHorizon (eyePnt, edge.getOpposite(),
				      oppFace, horizon);
		  }
		 else
		  { horizon.add (edge);
		    if (debug)
		     { System.out.println ("  adding horizon edge " +
					   edge.getVertexString());
		     }
		  }
	       }
	      edge = edge.getNext();
	    }
	   while (edge != edge0);
	 }

	private HalfEdge addAdjoiningFace (
	   Vertex eyeVtx, HalfEdge he)
	 { 
	   Face face = Face.createTriangle (
	      eyeVtx, he.tail(), he.head());
 	   faces.add (face);
	   face.getEdge(-1).setOpposite(he.getOpposite());
	   return face.getEdge(0);
	 }

	protected void addNewFaces (
	   FaceList newFaces, Vertex eyeVtx, Vector horizon)
	 { 
	   newFaces.clear();

	   HalfEdge hedgeSidePrev = null;
	   HalfEdge hedgeSideBegin = null;

	   for (Iterator it=horizon.iterator(); it.hasNext(); ) 
	    { HalfEdge horizonHe = (HalfEdge)it.next();
	      HalfEdge hedgeSide = addAdjoiningFace (eyeVtx, horizonHe);
	      if (debug)
	       { System.out.println (
		    "new face: " + hedgeSide.face.getVertexString());
	       }
	      if (hedgeSidePrev != null)
	       { hedgeSide.next.setOpposite (hedgeSidePrev);		 
	       }
	      else
	       { hedgeSideBegin = hedgeSide; 
	       }
	      newFaces.add (hedgeSide.getFace());
	      hedgeSidePrev = hedgeSide;
	    }
	   hedgeSideBegin.next.setOpposite (hedgeSidePrev);
	 }

	protected Vertex nextPointToAdd()
	 {
	   if (!claimed.isEmpty())
               
	    { Face eyeFace = claimed.first().face;
	      Vertex eyeVtx = null;
	      double maxDist = 0;
	      for (Vertex vtx=eyeFace.outside;
		   vtx != null && vtx.face==eyeFace;
		   vtx = vtx.next)
	       { double dist = eyeFace.distanceToPlane(vtx.pnt);
		 if (dist > maxDist)
		  { maxDist = dist;
		    eyeVtx = vtx;
		  }
	       }
	      return eyeVtx;
	    }
	   else
	    { return null;
	    }
	 }
	
	protected void addPointToHull(Vertex eyeVtx)
	 {
	     horizon.clear();
	     unclaimed.clear();
	      
	     if (debug)
	      { System.out.println ("Adding point: " + eyeVtx.pnt.toString());
		System.out.println (
		   " which is " + eyeVtx.face.distanceToPlane(eyeVtx.pnt) +
		   " above face " + eyeVtx.face.getVertexString());
	      }
	     removePointFromFace (eyeVtx, eyeVtx.face);
	     calculateHorizon (eyeVtx.pnt, null, eyeVtx.face, horizon);
	     
             newFaces.clear();
	     addNewFaces (newFaces, eyeVtx, horizon);
	     
	     // first merge pass ... merge faces which are non-convex
	     // as determined by the larger face
	     
	     for (Face face = newFaces.first(); face!=null; face=face.next)
	      { 
		if (face.mark == Face.VISIBLE)
		 { while (doAdjacentMerge(face, NONCONVEX_WRT_LARGER_FACE))
		      ;
		 }
	      }		 
	     // second merge pass ... merge faces which are non-convex
	     // wrt either face	     
	     for (Face face = newFaces.first(); face!=null; face=face.next)
	      { 
 		if (face.mark == Face.NON_CONVEX)
		 { face.mark = Face.VISIBLE;
		   while (doAdjacentMerge(face, NONCONVEX))
		      ;
 		 }
 	      }	
	     resolveUnclaimedPoints(newFaces);
	 }

	protected void buildHull ()
	 {
	   int cnt = 0;
	   Vertex eyeVtx;

	   computeMaxAndMin ();
	   createInitialSimplex ();
	   while ((eyeVtx = nextPointToAdd()) != null)
	    { addPointToHull (eyeVtx);
	      cnt++;
/******************************************************************************/	      
          int k=0;
          
          for (int i=0;i<faces.size();i++){
          faces.get(i).Id=i;
          }
              
              
/******************************************************************************/              
              
              if (debug)
	       { System.out.println ("iteration " + cnt + " done"); 
	       }
	    
            
            }
	   reindexFacesAndVertices();
	   if (debug)
	    { System.out.println ("hull done");
	    }
           
           
        System.out.println("-------------------------------------------------");
        
        System.out.println("The Faces Of The Hull");
        int k=0;
        for (Iterator it=faces.iterator(); it.hasNext(); )
	    { Face face = (Face)it.next();
          String S= face.he0.vertex.pnt.toString() +" - ";
          S+= face.he0.next.vertex.pnt.toString()+" - ";
          S+=face.he0.next.next.vertex.pnt.toString();
         
          System.out.println("Face>>"+k+">>>> "+S);
          k++;
        }  
        System.out.println("-------------------------------------------------");
        
           
           
	 }

	private void markFaceVertices (Face face, int mark)
	 {
	   HalfEdge he0 = face.getFirstEdge();
	   HalfEdge he = he0;
	   do
	    { he.head().index = mark;
	      he = he.next;
	    }
	   while (he != he0);
	 }

	protected void reindexFacesAndVertices()
	 { 
	   for (int i=0; i<numPoints; i++)
	    { pointBuffer[i].index = -1; 
	    }
	   // remove inactive faces and mark active vertices
	   numFaces = 0;
	   for (Iterator it=faces.iterator(); it.hasNext(); )
	    { Face face = (Face)it.next();
	      if (face.mark != Face.VISIBLE)
	       { it.remove();
	       }
	      else
	       { markFaceVertices (face, 0);
		 numFaces++;
	       }
	    }
	   // reindex vertices
	   numVertices = 0;
	   for (int i=0; i<numPoints; i++)
	    { Vertex vtx = pointBuffer[i];
	      if (vtx.index == 0)
	       { vertexPointIndices[numVertices] = i;
		 vtx.index = numVertices++;
	       }
	    }
	 }

	protected boolean checkFaceConvexity (
	   Face face, double tol, PrintStream ps)
	 {
	   double dist;
	   HalfEdge he = face.he0;
	   do
	    { face.checkConsistency();
	      // make sure edge is convex
	      dist = oppFaceDistance (he);
	      if (dist > tol)
	       { if (ps != null)
		  { ps.println ("Edge " + he.getVertexString() +
				" non-convex by " + dist);
		  }
		 return false;
	       }
	      dist = oppFaceDistance (he.opposite);
	      if (dist > tol)
	       { if (ps != null)
		  { ps.println ("Opposite edge " +
				he.opposite.getVertexString() +
				" non-convex by " + dist);
		  }
		 return false;
	       }
	      if (he.next.oppositeFace() == he.oppositeFace())
	       { if (ps != null)
		  { ps.println ("Redundant vertex " + he.head().index +
				" in face " + face.getVertexString());
		  }
		 return false;
	       }
	      he = he.next;
	    }
	   while (he != face.he0);	   
	   return true;
	 }

	protected boolean checkFaces(double tol, PrintStream ps)
	 { 
	   // check edge convexity
	   boolean convex = true;
	   for (Iterator it=faces.iterator(); it.hasNext(); ) 
	    { Face face = (Face)it.next();
	      if (face.mark == Face.VISIBLE)
	       { if (!checkFaceConvexity (face, tol, ps))
		  { convex = false;
		  }
	       }
	    }
	   return convex;
	 }

	/**
	 * Checks the correctness of the hull using the distance tolerance
	 * returned by {@link QuickHull3D#getDistanceTolerance
	 * getDistanceTolerance}; see
	 * {@link QuickHull3D#check(PrintStream,double)
	 * check(PrintStream,double)} for details.
	 *
	 * @param ps print stream for diagnostic messages; may be
	 * set to <code>null</code> if no messages are desired.
	 * @return true if the hull is valid
	 * @see QuickHull3D#check(PrintStream,double)
	 */
	public boolean check (PrintStream ps)
	 {
	   return check (ps, getDistanceTolerance());
	 }

	/**
	 * Checks the correctness of the hull. This is done by making sure that
	 * no faces are non-convex and that no points are outside any face.
	 * These tests are performed using the distance tolerance <i>tol</i>.
	 * Faces are considered non-convex if any edge is non-convex, and an
	 * edge is non-convex if the centroid of either adjoining face is more
	 * than <i>tol</i> above the plane of the other face. Similarly,
	 * a point is considered outside a face if its distance to that face's
	 * plane is more than 10 times <i>tol</i>.
	 *
	 * <p>If the hull has been {@link #triangulate triangulated},
	 * then this routine may fail if some of the resulting
	 * triangles are very small or thin.
	 *
	 * @param ps print stream for diagnostic messages; may be
	 * set to <code>null</code> if no messages are desired.
	 * @param tol distance tolerance
	 * @return true if the hull is valid
	 * @see QuickHull3D#check(PrintStream)
	 */
	public boolean check (PrintStream ps, double tol)

	 {
	   // check to make sure all edges are fully connected
	   // and that the edges are convex
	   double dist;
	   double pointTol = 10*tol;

	   if (!checkFaces(tolerance, ps))
	    { return false; 
	    }

	   // check point inclusion

	   for (int i=0; i<numPoints; i++)
	    { Point3d pnt = pointBuffer[i].pnt;
	      for (Iterator it=faces.iterator(); it.hasNext(); ) 
	       { Face face = (Face)it.next();
		 if (face.mark == Face.VISIBLE)
		  { 
		    dist = face.distanceToPlane (pnt);
		    if (dist > pointTol)
		     { if (ps != null)
			{ ps.println (
			     "Point " + i + " " + dist + " above face " +
			     face.getVertexString());
			}
		       return false;
		     }
		  }
	       }
	    }
	   return true;
	 }

     
     
     
   //***************************************************************************  
   
    public void reduce(Text key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
        
        ArrayList<Point3d> Points = new ArrayList<Point3d>();
        
        for(Text val:values){
            String line = val.toString();
        
             if (line.length()>1){
                 //String []linesArr = line.split("_");
                 //String[] S=linesArr[1].split(" ");
                 
                 String[] S=line.split(" ");
                 Point3d TVertex=new Point3d();
            
                 
                 TVertex.x=Double.valueOf(S[0]);
                 TVertex.y=Double.valueOf(S[1]);
                 TVertex.z=Double.valueOf(S[2]);
                 Points.add(TVertex);
             
             
             }
            
        }
        
        
       
/******************************************************************************/
        
        
        Point3d[] PointsArray=new Point3d[Points.size()];
        build(Points.toArray(PointsArray));
        
       
        
        
        
        for (int i=0;i<faces.size();i++){
         
            String S= faces.get(i).he0.vertex.pnt.toString() +" - ";
          S+= faces.get(i).he0.next.vertex.pnt.toString()+" - ";
          S+=faces.get(i).he0.next.next.vertex.pnt.toString();
         
            
            
            context.write(null,new Text("Face>>"+i+">>>> "+S));
        
        }
        
        
        
              
        
        
        
       
        
    }
  }
         
 
 
 
/******************************************************************************/
 
 
 
 
 
 
 

 static String [] HDFSRead(String FPath, int size)throws Exception{
 
     String[] SArray=new String[size];
     
     int k=0;
     //Path pt=new Path("hdfs://Angel:9000/output200/part-r-00000");
     Path pt=new Path(FPath);
     FileSystem fs = FileSystem.get(new Configuration());
                        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
                        String line;
                        line=br.readLine();
                        SArray[k++]=line;         
                        
                        
                        
                        while (line != null){
                                System.out.println("><"+line);
                                line=br.readLine();
                                SArray[k++]=line;
                        
                        }
                        
     
     
     return SArray;
 
 
 
 } 
 /*****************************************************************************/
 
 static String  HDFSRead(String FPath)throws Exception{
 
     Path pt=new Path(FPath);
     FileSystem fs = FileSystem.get(new Configuration());
                        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
                        String line;
                       return line=br.readLine();
     
 
 
 } 
 /*****************************************************************************/
 
 static  String FacesToS(Face[] FacesArray){
   String FS="";
   
   for(int i=0;i<FacesArray.length;i++){
       if(FacesArray[i].mark==Face.VISIBLE){
   Vector3d normal=FacesArray[i].getNormal();
   FS+=FacesArray[i].Id+" "+normal.toString()+" "+FacesArray[i].planeOffset+"_";
       }
   
   }
   
   return FS;
   
   
   }
 
 
 
  static  String FacesToS(ArrayList<Face> FacesArray){
   String FS="";
   
   for(int i=0;i<FacesArray.size();i++){
       if (FacesArray.get(i).mark==Face.VISIBLE){
   Vector3d normal=FacesArray.get(i).getNormal();
   FS+=FacesArray.get(i).Id+" "+normal.toString()+" "+FacesArray.get(i).planeOffset+" ";
   FS+=FacesArray.get(i).he0.vertex.pnt.toString()+" "+FacesArray.get(i).he0.next.vertex.pnt.toString()+" ";
   FS+=FacesArray.get(i).he0.next.next.vertex.pnt.toString()+"_";
       }
   }
   
   return FS;
   
   
   }
 
/******************************************************************************/
 
	protected static void calculateHorizon (
	   Point3d eyePnt, HalfEdge edge0, Face face, Vector horizon)
	 {
//	   oldFaces.add (face);
	   //deleteFacePoints (face, null); Do it by a Job // Delete all the points from the face
           // if there are more points add it to the unclaimed list
             
 	   face.mark = Face.DELETED;
	   
           
	   System.out.println ("  visiting face " + face.toString());
	    
	   
           HalfEdge edge;
	   if (edge0 == null)
	    { edge0 = face.getEdge(0);
	      edge = edge0;
            //  System.out.println("face.GetEdge0>>"+edge.vertex.pnt.toString()+">>"+edge.tail().pnt.toString());
	    }
	   else
	    { edge = edge0.getNext();
	    
           // System.out.println("face.GetEdge0.getnext>>"+edge.vertex.pnt.toString()+">>"+edge.tail().pnt.toString());

            }
	   do
	    { Face oppFace = edge.oppositeFace();
          

	      if (oppFace.mark == Face.VISIBLE)
	       { 
             //       System.out.println("oppface.disttoplan>tolerance>>"+(oppFace.distanceToPlane (eyePnt) > tolerance));
                   if (oppFace.distanceToPlane (eyePnt) > tolerance)
                   
                   
                   
		  { calculateHorizon (eyePnt, edge.getOpposite(),
				      oppFace, horizon);
		  }
		 else
		  { horizon.add (edge);
		    
		      System.out.println ("  adding horizon edge " +
					   edge.vertex.pnt.toString()+"-"+edge.tail().pnt.toString());
		     
		  }
	       }
	      edge = edge.getNext();
	    }
	   while (edge != edge0);
	 }


 
    
        private static HalfEdge addAdjoiningFace (
	   Vertex eyeVtx, HalfEdge he)
	 { 
	   Face face = Face.createTriangle (
	      eyeVtx, he.tail(), he.head());
 	   faces.add (face);
	   face.getEdge(-1).setOpposite(he.getOpposite());
	   return face.getEdge(0);
	 }

        
        

	protected static void addNewFaces (
	   FaceList newFaces, Vertex eyeVtx, Vector horizon)
	 { 
	   newFaces.clear();

	   HalfEdge hedgeSidePrev = null;
	   HalfEdge hedgeSideBegin = null;

	   for (Iterator it=horizon.iterator(); it.hasNext(); ) 
	    { HalfEdge horizonHe = (HalfEdge)it.next();
	      HalfEdge hedgeSide = addAdjoiningFace (eyeVtx, horizonHe);
	      
	        System.out.println (
		    "new face: " + hedgeSide.face.toString());
	       
	      if (hedgeSidePrev != null)
	       { hedgeSide.next.setOpposite (hedgeSidePrev);		 
	       }
	      else
	       { hedgeSideBegin = hedgeSide; 
	       }
	      newFaces.add (hedgeSide.getFace());
	      hedgeSidePrev = hedgeSide;
	    }
	   hedgeSideBegin.next.setOpposite (hedgeSidePrev);
	 }

        
        protected static double oppFaceDistance (HalfEdge he)
	 {
	   return he.face.distanceToPlane (he.opposite.face.getCentroid());
	 }

        
        
        
	private static boolean doAdjacentMerge (Face face, int mergeType)
	 {
	   HalfEdge hedge = face.he0;

	   boolean convex = true;
	   do
	    { Face oppFace = hedge.oppositeFace();
	      boolean merge = false;
	      double dist1, dist2;

	      if (mergeType == NONCONVEX)
	       { // then merge faces if they are definitively non-convex
		 if (oppFaceDistance (hedge) > -tolerance ||
		     oppFaceDistance (hedge.opposite) > -tolerance)
		  { merge = true;
		  }
	       }
	      else // mergeType == NONCONVEX_WRT_LARGER_FACE
	       { // merge faces if they are parallel or non-convex
		 // wrt to the larger face; otherwise, just mark
		 // the face non-convex for the second pass.
		 if (face.area > oppFace.area)
		  { if ((dist1 = oppFaceDistance (hedge)) > -tolerance) 
		     { merge = true;
		     }
		    else if (oppFaceDistance (hedge.opposite) > -tolerance)
		     { convex = false;
		     }
		  }
		 else
		  { if (oppFaceDistance (hedge.opposite) > -tolerance)
		     { merge = true;
		     }
		    else if (oppFaceDistance (hedge) > -tolerance) 
		     { convex = false;
		     }
		  }
	       }

	      if (merge)
	       { 
		   System.out.println (
		    "  merging " + face.getVertexString() + "  and  " +
		    oppFace.getVertexString());
		  

		 int numd = face.mergeAdjacentFace (hedge, discardedFaces);
		 for (int i=0; i<numd; i++)
		  { 
                      
                  // deleteFacePoints (discardedFaces[i], face);
		  // Do it at a Job
                  
                  }
		 
		   System.out.println (
		       "  result: " + face.getVertexString());
		  
		 return true;
	       }
	      hedge = hedge.next;
	    }
	   while (hedge != face.he0);
	   if (!convex)
	    { face.mark = Face.NON_CONVEX; 
	    }
	   return false;
	 }


        
       
 protected static void addPointToHull(Vertex eyeVtx, Face EyeFace)
	 {
	     horizon.clear();
	     //unclaimed.clear();
	      
	       // eyeVtx.face=EyeFace;
	        System.out.println ("Adding point: " + eyeVtx.toString());
		System.out.println (
		   " which is " + EyeFace.distanceToPlane(eyeVtx.pnt) +
		   " above face " + EyeFace.toString());
	      
	    // removePointFromFace (eyeVtx, eyeVtx.face);// Do it by a Job
                
	     calculateHorizon (eyeVtx.pnt, null, EyeFace, horizon);
            
             
              
             
	     newFaces.clear();
	     addNewFaces (newFaces, eyeVtx, horizon);
	     
	     // first merge pass ... merge faces which are non-convex
	     // as determined by the larger face
	     
	     for (Face face = newFaces.first(); face!=null; face=face.next)
	      { 
		if (face.mark == Face.VISIBLE)
		 { while (doAdjacentMerge(face, NONCONVEX_WRT_LARGER_FACE))
		      ;
		 }
	      }		 
	     // second merge pass ... merge faces which are non-convex
	     // wrt either face	     
	     for (Face face = newFaces.first(); face!=null; face=face.next)
	      { 
 		if (face.mark == Face.NON_CONVEX)
		 { face.mark = Face.VISIBLE;
		   while (doAdjacentMerge(face, NONCONVEX))
		      ;
 		 }
 	      }	
	    // resolveUnclaimedPoints(newFaces); Do it by a Job // Get the unclaimed points
            // and test them with the new faces. Or Delete them if they are not outside the faces 
             
 	 }

 
 
 
//****************************************************************************// 
  protected static void BuildHull (long RC)
	  throws Exception{
      
      
    int It=0;
    //System.out.println("Counter>>"+RC);
    boolean exit; 
    if (RC>1) {  
      exit=false; }else{ exit=true;}    
          
          
          
          
          
    String Input="/AssignPoints1";
    String Output1="/FindFP";
    String Output2="/AssignPoints2";
    //String Output3="/output700";
          
    while(exit==false){
       It++;
          
       System.out.println("");
       System.out.println("ITERATION NO >>>"+It);
       System.out.println("");
          
              
              
              
              
              
       String []Tline=HDFSRead("hdfs://192.168.0.206:9000"+Input+"/part-r-00000").split("_");    
       String FName=Tline[0];
       //System.out.println("FName>>"+FName);    
       Face EyeFace=new Face();
          
       for (int i=0; i<faces.size();i++){
          
        //  System.out.println("FNameArray>>"+faces.get(i).toString());
        //  System.out.println("Fnrml>>"+faces.get(i).getNormal().toString());
          
              
          if ((String.valueOf(faces.get(i).Id)).equalsIgnoreCase(FName)){
             
          EyeFace=faces.get(i);}
          
          }
          
          Vector3d TempV=EyeFace.getNormal();
          String Faceinfo=TempV.x+" "+TempV.y+" "+TempV.z+" "+EyeFace.planeOffset;
          //System.out.println("FaceInfo>>"+Faceinfo);
          
          
          JobConf conf5 = new JobConf();
          // Important note // All the points chunks have to be > 2 points
          conf5.setInt("n.lines.records.token", 100); 
          conf5.set("Faceinfo", Faceinfo);           
          conf5.set("FaceName", FName);
          conf5.setNumTasksToExecutePerJvm(-1);
      
    
          Job job5 = new Job(conf5, "FindFP");
          job5.setJarByClass(MRQuickhull.class);
          job5.setMapperClass(FindFPMapper.class);
    
    
          job5.setInputFormatClass(NLinesInputFormat.class);  
          job5.setReducerClass(FindFPReducer.class);
          job5.setMapOutputKeyClass(Text.class);
          job5.setMapOutputValueClass(Text.class);
          FileInputFormat.addInputPath(job5, new Path(Input));
          FileOutputFormat.setOutputPath(job5, new Path(Output1));
    
          job5.waitForCompletion(true);

          String[] EyeVertexArray=HDFSRead("hdfs://192.168.0.206:9000"+Output1+"/part-r-00000").split(" ");
          Vertex EyeVertex=new Vertex();
          
            
          FileSystem fs = FileSystem.get(new Configuration());  
          fs.delete(new Path("hdfs://192.168.0.206:9000"+Output1+"/"), true); // delete file, true for recursive 
          
          
          EyeVertex.pnt.x=Double.valueOf(EyeVertexArray[0]);
          EyeVertex.pnt.y=Double.valueOf(EyeVertexArray[1]);
          EyeVertex.pnt.z=Double.valueOf(EyeVertexArray[2]);
          
          //System.out.println("EyeVertex>>"+EyeVertex.pnt.toString());
          
          addPointToHull( EyeVertex, EyeFace);
         
          for (int i=0; i<faces.size();i++){
             faces.get(i).Id=i;
          }
          
          
          
         String FS=FacesToS(faces);
         //System.out.println("FacesToS>>"+FS);
         String HorizonFS="";
         for (Iterator it=horizon.iterator(); it.hasNext(); )
	    
            { HalfEdge edge = (HalfEdge)it.next();
            
            HorizonFS+=edge.face.Id+"_";
            
            }
          
          
          //System.out.println("HorizonFS>>"+HorizonFS);
      
          JobConf conf6 = new JobConf();
           // Important note // All the points chunks have to be > 2 points
          conf6.setInt("n.lines.records.token", 1); 
          conf6.setStrings("FacesS",FS);
          conf6.setStrings("HorizonFS",HorizonFS);
          conf6.setStrings("toleranceS",String.valueOf(tolerance));
          
               
          conf6.setNumTasksToExecutePerJvm(-1);
      
    
          Job job6 = new Job(conf6, "AssignPoints2");
          job6.setJarByClass(MRQuickhull.class);
          job6.setMapperClass(AssignP2Mapper.class);
    
    
          job6.setInputFormatClass(NLinesInputFormat.class);  
          job6.setReducerClass(AssignP2Reducer.class);
          job6.setMapOutputKeyClass(Text.class);
          job6.setMapOutputValueClass(Text.class);
          FileInputFormat.addInputPath(job6, new Path(Input));
          FileOutputFormat.setOutputPath(job6, new Path(Output2));
    
          job6.waitForCompletion(true);
    
      
          // Make sure that there is no white spaces at the end of the file
      
           
          //FileSystem fs = FileSystem.get(new Configuration());  
          fs.delete(new Path("hdfs://192.168.0.206:9000"+Input+"/"), true); // delete file, true for recursive 
          
          Input=Output2;
          Output1="/FindFP"+"-"+It;
          Output2="/AssignPoints2"+"-"+It; 
          // Output3+="1"; 
        
         
         
         
          org.apache.hadoop.mapreduce.Counters counters = job6.getCounters();
          
          long CounterValue=counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "REDUCE_OUTPUT_RECORDS").getValue();
             
          if (CounterValue<1 ){
        
             exit=true; 
          }  
        
        
        
   }
          
          
          
      for (Iterator it=faces.iterator(); it.hasNext(); )
	    { Face face = (Face)it.next();
	      if (face.mark != Face.VISIBLE)
	       { it.remove();
	       }
	      
	    }
        
        
        System.out.println("-------------------------------------------------");
        
        System.out.println("The Faces Of The Hull");
        
        for (int i=0;i<faces.size();i++){
          String S= faces.get(i).he0.vertex.pnt.toString() +" - ";
          S+= faces.get(i).he0.next.vertex.pnt.toString()+" - ";
          S+=faces.get(i).he0.next.next.vertex.pnt.toString();
         
          System.out.println("Face>>"+i+">>>> "+S);
        }  
        System.out.println("-------------------------------------------------");
        

      
      
      
      
      
      
          
     
  
  }
 
 
   protected static void createInitialSimplex (Vertex[]vtx,Vector3d nrml, double d0, String TempDir1)
	  throws Exception{
   
   
	System.out.println ("initial vertices:");
	System.out.println (vtx[0].index + ": " + vtx[0].pnt);
	System.out.println (vtx[1].index + ": " + vtx[1].pnt);
	System.out.println (vtx[2].index + ": " + vtx[2].pnt);
	System.out.println (vtx[3].index + ": " + vtx[3].pnt);
	    

	Face[] tris = new Face[4];

	if (vtx[3].pnt.dot (nrml) - d0 < 0)
	    { tris[0] = Face.createTriangle (vtx[0], vtx[1], vtx[2]);
	      tris[1] = Face.createTriangle (vtx[3], vtx[1], vtx[0]);
	      tris[2] = Face.createTriangle (vtx[3], vtx[2], vtx[1]);
	      tris[3] = Face.createTriangle (vtx[3], vtx[0], vtx[2]);

	      for (int i=0; i<3; i++)
	       { int k = (i+1)%3;
		 tris[i+1].getEdge(1).setOpposite (tris[k+1].getEdge(0));
		 tris[i+1].getEdge(2).setOpposite (tris[0].getEdge(k));
	       }
	    }
	else
	    { tris[0] = Face.createTriangle (vtx[0], vtx[2], vtx[1]);
	      tris[1] = Face.createTriangle (vtx[3], vtx[0], vtx[1]);
	      tris[2] = Face.createTriangle (vtx[3], vtx[1], vtx[2]);
	      tris[3] = Face.createTriangle (vtx[3], vtx[2], vtx[0]);

	      for (int i=0; i<3; i++)
	       { int k = (i+1)%3;
		 tris[i+1].getEdge(0).setOpposite (tris[k+1].getEdge(1));
		 tris[i+1].getEdge(2).setOpposite (tris[0].getEdge((3-i)%3));
	       }
	   }


 	 for (int i=0; i<4; i++)
 	    { 
                // System.out.println("Face "+i+">>"+tris[i].toString());
                faces.add (tris[i]);
                faces.get(i).Id=i;
 	    }

        String FS=FacesToS(tris);
        //System.out.println("FacesToS>>"+FS);
      
      
        JobConf conf4 = new JobConf();
        // Important note // All the points chunks have to be > 2 points
        conf4.setInt("n.lines.records.token", 1); 
        conf4.setStrings("FacesS",FS);
        conf4.setStrings("toleranceS",String.valueOf(tolerance));
          
        conf4.setStrings("vtx0S",vtx[0].pnt.toString());
        conf4.setStrings("vtx1S",vtx[1].pnt.toString());
        conf4.setStrings("vtx2S",vtx[2].pnt.toString());
        conf4.setStrings("vtx3S",vtx[3].pnt.toString());
           
           
        conf4.setNumTasksToExecutePerJvm(-1);
      
    
        Job job4 = new Job(conf4, "AssignPoints1");
        job4.setJarByClass(MRQuickhull.class);
        job4.setMapperClass(AssignP1Mapper.class);
    
    
        job4.setInputFormatClass(NLinesInputFormat.class);  
        job4.setReducerClass(AssignP1Reducer.class);
        job4.setMapOutputKeyClass(Text.class);
        job4.setMapOutputValueClass(Text.class);
        job4.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job4, new Path(TempDir1));
        FileOutputFormat.setOutputPath(job4, new Path("/AssignPoints1"));
    
        job4.waitForCompletion(true);
    
      
        // Make sure that there is no white spaces at the end of the file
      
 /*****************************************************************************/     
        org.apache.hadoop.mapreduce.Counters counters = job4.getCounters();
          
        long CounterValue=counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "REDUCE_OUTPUT_RECORDS").getValue();
    
        BuildHull(CounterValue);
          
          
          
          
   } 
   
   
  
/******************************************************************************/
   
  public static void main(String[] args) throws Exception {
    
     
     /*
      System.out.println("*************************************");
      System.out.println("Using The Hybird Quick Hull Algorithm");
      System.out.println("*************************************");
      
      int count=0;
      long CurrentCount=-3;
      long PreviousCount=-6;
      
      boolean FastPass=false;
      String TempDir1="/input";
      String TempDir2="/QH";
      
           
      while (CurrentCount!=PreviousCount) {
           
          count++;
          PreviousCount=CurrentCount;
   
          
          System.out.println("*************************************");
          System.out.println("Hybird Quick Hull Iteration >> "+count);
          System.out.println("*************************************");
      
          
          
          JobConf conf7 = new JobConf();
           // Important note // All the points chunks have to be > 2 points
          conf7.setInt("n.lines.records.token", 100); 
          conf7.setNumTasksToExecutePerJvm(-1);
      
    
          Job job7 = new Job(conf7, "QH");
          job7.setJarByClass(MRQuickhull.class);
          job7.setMapperClass(QHMapper.class);
    
    
          job7.setInputFormatClass(NLinesInputFormat.class);  
          job7.setReducerClass(QHReducer.class);
          job7.setMapOutputKeyClass(Text.class);
          job7.setMapOutputValueClass(Text.class);
          FileInputFormat.addInputPath(job7, new Path(TempDir1));
          FileOutputFormat.setOutputPath(job7, new Path(TempDir2));
    
          job7.waitForCompletion(true);
          
          if (count!=1){
            FileSystem fs = FileSystem.get(new Configuration());  
            fs.delete(new Path("hdfs://192.168.0.206:9000"+TempDir1+"/"), true); // delete file, true for recursive 
          }
          
          TempDir1=TempDir2;
          TempDir2="/QH"+"-"+count;
      
          org.apache.hadoop.mapreduce.Counters counters = job7.getCounters();
          
          long OutputRec=counters.findCounter("org.apache.hadoop.mapred.Task$Counter", "REDUCE_OUTPUT_RECORDS").getValue();
     
          CurrentCount=OutputRec;
          
          if ((OutputRec<=30000000)&&(OutputRec!=0)){
           
              FastPass=true;
            break;
          }
          
          
          
          
      
      }     
      
          
      if (FastPass==true){
      

          JobConf conf8 = new JobConf();
          // Important note // All the points chunks have to be > 2 points
          conf8.setInt("n.lines.records.token", 1); 
          conf8.setNumTasksToExecutePerJvm(-1);
      
    
          Job job8 = new Job(conf8, "FinalQH");
          job8.setJarByClass(MRQuickhull.class);
          job8.setMapperClass(FinalQHMapper.class);
    
    
          job8.setInputFormatClass(NLinesInputFormat.class);  
          job8.setReducerClass(FinalQHReducer.class);
          job8.setMapOutputKeyClass(Text.class);
          job8.setMapOutputValueClass(Text.class);
          FileInputFormat.addInputPath(job8, new Path(TempDir1));
          FileOutputFormat.setOutputPath(job8, new Path("/Convex-Hull-Faces"));
    
          job8.waitForCompletion(true);

          
      
      }
      
      else{  */
       
    
        System.out.println("*******************************************");
        System.out.println("Using MapReduce Quick Hull Step By Step !!!");
        System.out.println("*******************************************");
          
        String TempDir1="/input/3.txt";  
      
        String MaxMinS="=";
        String MaxS="";
        String MinS="";
        JobConf conf1 = new JobConf();
        conf1.setInt("n.lines.records.token", 100);
        conf1.set("MaxMinS",MaxMinS);
        conf1.setStrings("MaxS",MaxS);
        conf1.setStrings("MinS",MinS);
        conf1.setNumTasksToExecutePerJvm(-1);
      
    
        Job job1 = new Job(conf1, "MaxMin1");
        job1.setJarByClass(MRQuickhull.class);
        job1.setMapperClass(MaxMin1Mapper.class);
    
    
        job1.setInputFormatClass(NLinesInputFormat.class);
   
    
        job1.setReducerClass(MaxMin1Reducer.class);
        job1.setMapOutputKeyClass(Text.class);
        job1.setMapOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job1, new Path(TempDir1));
        FileOutputFormat.setOutputPath(job1, new Path("/MaxMin1"));
    
    
   
        job1.waitForCompletion(true);
    
        String []TSArray=new String[4];
        int k=0;
        Path pt=new Path("hdfs://192.168.0.206:9000/MaxMin1/part-r-00000");
                        FileSystem fs = FileSystem.get(new Configuration());
                        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
                        String line;
                        line=br.readLine();
                        TSArray[k++]=line;         
                        
                        
                        
                        while (line != null){
                               // System.out.println("><"+line);
                                line=br.readLine();
                                TSArray[k++]=line;
                        
                        }
                        
                        
       for (int i=0; i<TSArray.length;i++){
       
          if (TSArray[i]!=null){
       
            String []S=TSArray[i].split("_");
            if (S[0].equalsIgnoreCase("MaxMinS")==true){
            MaxMinS=S[1];
           }
          else if (S[0].equalsIgnoreCase("MaxS")==true)
           {
               MaxS=S[1];
           
           }
           else{ MinS=S[1];}
       
        }
       
       }                 
                        
        Vector3d max = new Vector3d();
        Vector3d min = new Vector3d();
    
        int MxI,MnI;
        MxI=0;
        MnI=0;
     
        String []linesArr = MaxMinS.split("=");
     
        for (int i=0; i<linesArr.length;i++){
        
            String[] S=linesArr[i].split(" ");
            if (S.length>=3){
                Vertex TVertex=new Vertex();
            
            if (i%2!=0){
                TVertex.pnt.x=Double.valueOf(S[0]);
                TVertex.pnt.y=Double.valueOf(S[1]);
                TVertex.pnt.z=Double.valueOf(S[2]);
                maxVtxs[MxI++]=TVertex; 
            
                }
            
            else{
                TVertex.pnt.x=Double.valueOf(S[0]);
                TVertex.pnt.y=Double.valueOf(S[1]);
                TVertex.pnt.z=Double.valueOf(S[2]);
                minVtxs[MnI++]=TVertex; 
            
            
                
                }
            
                }     
            }
   
     
            String[] S=MaxS.split(" ");
            if (S.length>=3){
             Vector3d TVertex=new Vector3d();
                       
             TVertex.x=Double.valueOf(S[0]);
             TVertex.y=Double.valueOf(S[1]);
             TVertex.z=Double.valueOf(S[2]);
             
             max=TVertex; 
            }
         S=MinS.split(" ");
          if (S.length>=3){
             Vector3d TVertex=new Vector3d();
                       
             TVertex.x=Double.valueOf(S[0]);
             TVertex.y=Double.valueOf(S[1]);
             TVertex.z=Double.valueOf(S[2]);
             
             min=TVertex; 
            }
 
        // this epsilon formula comes from QuickHull, and I'm
	// not about to quibble.
	// We use the charLength at the triangulate function
        charLength = Math.max(max.x-min.x, max.y-min.y);
	charLength = Math.max(max.z-min.z, charLength);
	if (explicitTolerance == AUTOMATIC_TOLERANCE)
	    { tolerance =
		 3*DOUBLE_PREC*(Math.max(Math.abs(max.x),Math.abs(min.x))+
				Math.max(Math.abs(max.y),Math.abs(min.y))+
				Math.max(Math.abs(max.z),Math.abs(min.z)));
	    }
	   else
	    { tolerance = explicitTolerance; 
	    }
      
//****************************************************************************// 
     // System.out.println("MaxMinS>>))"+MaxMinS);
     // System.out.println("Max>>))"+max.toString());
     // System.out.println("Min>>))"+min.toString());
     // System.out.println("Maxvts0>>"+maxVtxs[0].pnt.toString());
     // System.out.println("Maxvts1>>"+maxVtxs[1].pnt.toString());
     // System.out.println("Maxvts2>>"+maxVtxs[2].pnt.toString());
     // System.out.println("Minvts0>>"+minVtxs[0].pnt.toString());
     // System.out.println("Maxvts1>>"+minVtxs[1].pnt.toString());
     // System.out.println("Maxvts2>>"+minVtxs[2].pnt.toString());
      
//****************************************************************************// 

        double max2 = 0;
	int imax = 0;

	for (int i=0; i<3; i++)
	    { double diff = maxVtxs[i].pnt.get(i)-minVtxs[i].pnt.get(i);
	      if (diff > max2)
	       { max2 = diff;
		 imax = i;
	       }
 	    }

	if (max2 <= tolerance)
             { throw new IllegalArgumentException ("Input points appear to be coincident");
	    }
	Vertex[] vtx = new Vertex[4];
	// set first two vertices to be those with the greatest
	// one dimensional separation

	vtx[0] = maxVtxs[imax];
	vtx[1] = minVtxs[imax];

	// set third vertex to be the vertex farthest from
	// the line between vtx0 and vtx1
	Vector3d u01 = new Vector3d();
	Vector3d diff02 = new Vector3d();
	Vector3d nrml = new Vector3d();
	Vector3d xprod = new Vector3d();
	double maxSqr = 0;
	u01.sub (vtx[1].pnt, vtx[0].pnt);
	u01.normalize();
           
        String u01S=u01.toString();
        String vtx0S=vtx[0].pnt.toString();
        String vtx1S=vtx[1].pnt.toString();
        String xprodS=xprod.toString();
        String nrmlS=nrml.toString();
           
//****************************************************************************//	   
        JobConf conf2 = new JobConf();
        // Important note // All the points chunks have to be > 2 points
        conf2.setInt("n.lines.records.token", 100); 
        conf2.setStrings("u01S",u01S);
        conf2.setStrings("vtx0S",vtx0S);
        conf2.setStrings("vtx1S",vtx1S);
        conf2.setStrings("xprodS",xprodS);
        conf2.setStrings("nrmlS",nrmlS);
           
        conf2.setNumTasksToExecutePerJvm(-1);
      
    
        Job job2 = new Job(conf2, "MaxMin2");
        job2.setJarByClass(MRQuickhull.class);
        job2.setMapperClass(MaxMin2Mapper.class);
    
    
        job2.setInputFormatClass(NLinesInputFormat.class);  
        job2.setReducerClass(MaxMin2Reducer.class);
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job2, new Path(TempDir1));
        FileOutputFormat.setOutputPath(job2, new Path("/MaxMin2"));
    
        job2.waitForCompletion(true);
    
           
        //System.out.println("vtx0>>))"+vtx[0].pnt.toString());
        //System.out.println("vtx1>>))"+vtx[1].pnt.toString());
           
           
           
/******************************************************************************/      
        String MaxSqrS="";      
        String [] TArray=HDFSRead("hdfs://192.168.0.206:9000/MaxMin2/part-r-00000",4);      
     
        for (int i=0; i<TArray.length;i++){
            //System.out.print(TArray[i]);
            if (TArray[i]!=null){
       
                String []TS=TArray[i].split("_");
                // System.out.print(TS[1]);
           
                if (TS[0].equalsIgnoreCase("vtx2")==true){
                    String[] vtx2S=TS[1].split(" ");
                    Vertex TVertex=new Vertex();
                    TVertex.pnt.x=Double.valueOf(vtx2S[0]);
                    TVertex.pnt.y=Double.valueOf(vtx2S[1]);
                    TVertex.pnt.z=Double.valueOf(vtx2S[2]);
                    vtx[2]=TVertex;
                }
             else if (TS[0].equalsIgnoreCase("nrml")==true)
                {
           
                    String[] NrmlS=TS[1].split(" ");
                    nrml.x=Double.valueOf(NrmlS[0]);
                    nrml.y=Double.valueOf(NrmlS[1]);
                    nrml.z=Double.valueOf(NrmlS[2]);
           
                }
                   else{ 
               
                    MaxSqrS=TS[1];
                    }
       
             }
       
            }        
           
          //System.out.println("vtx2>>))"+vtx[2].pnt.toString());
          //System.out.println("nrml>>))"+nrml.toString());
          //System.out.println("maxsqr>>))"+MaxSqrS);
          
/******************************************************************************/
     
        maxSqr=Double.valueOf(MaxSqrS);
        
        //  System.out.println((Math.sqrt(maxSqr))+">>");
        //  System.out.println(100*tolerance);
          
          
         
        if (Math.sqrt(maxSqr) <= 100*tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be colinear");
	    }
	  
         nrml.normalize();

	 double maxDist = 0;
	 double d0 = vtx[2].pnt.dot (nrml);
	   
         String MaxDistS="0";
         String d0S=String.valueOf(d0);
         String vtx2S=vtx[2].pnt.toString();
         JobConf conf3 = new JobConf();
         // Important note // All the points chunks have to be > 2 points
         conf3.setInt("n.lines.records.token", 100); 
         conf3.setStrings("d0S",d0S);
         conf3.setStrings("MaxDistS",MaxDistS);
         conf3.setStrings("nrml",nrml.toString());
         conf3.setStrings("vtx0S",vtx0S);
         conf3.setStrings("vtx1S",vtx1S);
         conf3.setStrings("vtx2S",vtx2S);
           
         //conf2.setStrings("xprodS",xprodS);
         //conf2.setStrings("nrmlS",nrmlS);
           
         conf3.setNumTasksToExecutePerJvm(-1);
      
    
         Job job3 = new Job(conf3, "MaxMin3");
         job3.setJarByClass(MRQuickhull.class);
         job3.setMapperClass(MaxMin3Mapper.class);
    
    
         job3.setInputFormatClass(NLinesInputFormat.class);  
         job3.setReducerClass(MaxMin3Reducer.class);
         job3.setMapOutputKeyClass(Text.class);
         job3.setMapOutputValueClass(Text.class);
         FileInputFormat.addInputPath(job3, new Path(TempDir1));
         FileOutputFormat.setOutputPath(job3, new Path("/MaxMin3"));
    
         job3.waitForCompletion(true);
    
/******************************************************************************/
        String MaxDistS2="";      
        String [] TempArray=HDFSRead("hdfs://192.168.0.206:9000/MaxMin3/part-r-00000",3);      
     
        for (int i=0; i<TempArray.length;i++){
            //System.out.print(TArray[i]);
            if (TempArray[i]!=null){
       
                String []TempS=TempArray[i].split("_");
                // System.out.print(TS[1]);
           
                if (TempS[0].equalsIgnoreCase("vtx3S")==true){
                    String[] vtx3S=TempS[1].split(" ");
                    Vertex TVertex=new Vertex();
                    TVertex.pnt.x=Double.valueOf(vtx3S[0]);
                    TVertex.pnt.y=Double.valueOf(vtx3S[1]);
                    TVertex.pnt.z=Double.valueOf(vtx3S[2]);
                    vtx[3]=TVertex;
                 }
                    else if (TempS[0].equalsIgnoreCase("MaxDistS")==true)
                    {
                        MaxDistS2=TempS[1];
                    }
       
            }
       
           }        
     
     
     
     
          
      //System.out.println("vtx3>>))"+vtx[3].pnt.toString());
      //System.out.println("MaxDistS>>"+MaxDistS2);    
     
/******************************************************************************/ 
      
        maxDist=Double.valueOf(MaxDistS2);
     
        if (Math.abs(maxDist) <= 100*tolerance)
	    { throw new IllegalArgumentException ("Input points appear to be coplanar"); 
	    }

        createInitialSimplex (vtx,nrml,d0,TempDir1);
     
     
     
     
      
  
  }
  
}