package org.apache.giraph;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.giraph.examples.GeneratedVertexInputFormat;
import org.apache.giraph.examples.SimpleCheckpointVertex;
import org.apache.giraph.examples.SimpleVertexWriter;
import org.apache.giraph.examples.SuperstepBalancer;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for manual checkpoint restarting
 */
public class TestVertexRangeBalancer extends BspCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestVertexRangeBalancer(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TestVertexRangeBalancer.class);
    }

    /**
     * Run a sample BSP job locally and test how the vertex ranges are sent
     * from one worker to another.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public void testSuperstepBalancer()
        throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        setupConfiguration(conf);
        FileSystem hdfs = FileSystem.get(conf);
        conf.setClass(BspJob.VERTEX_CLASS,
                      SimpleCheckpointVertex.class,
                      HadoopVertex.class);
        conf.setClass(BspJob.VERTEX_INPUT_FORMAT_CLASS,
                      GeneratedVertexInputFormat.class,
                      VertexInputFormat.class);
        conf.setClass(BspJob.VERTEX_WRITER_CLASS,
                      SimpleVertexWriter.class,
                      VertexWriter.class);
        BspJob bspJob = new BspJob(conf, "testStaticBalancer");
        Path outputPath = new Path("/tmp/testStaticBalancer");
        hdfs.delete(outputPath, true);
        FileOutputFormat.setOutputPath(bspJob, outputPath);
        assertTrue(bspJob.run(true));
        if (getJobTracker() != null) {
            FileStatus [] fileStatusArr = hdfs.listStatus(outputPath);
            int totalLen = 0;
            for (FileStatus fileStatus : fileStatusArr) {
                if (fileStatus.getPath().toString().contains("/part-m-")) {
                    totalLen += fileStatus.getLen();
                }
            }
           assertTrue(totalLen == 118);
        }

        conf.setClass(BspJob.VERTEX_RANGE_BALANCER_CLASS,
                      SuperstepBalancer.class,
                      VertexRangeBalancer.class);
        BspJob bspJob2 = new BspJob(conf, "testSuperstepBalancer");
        outputPath = new Path("/tmp/testSuperstepBalancer");
        hdfs.delete(outputPath, true);
        FileOutputFormat.setOutputPath(bspJob2, outputPath);
        assertTrue(bspJob2.run(true));
        if (getJobTracker() != null) {
            FileStatus [] fileStatusArr = hdfs.listStatus(outputPath);
            int totalLen = 0;
            for (FileStatus fileStatus : fileStatusArr) {
                if (fileStatus.getPath().toString().contains("/part-m-")) {
                    totalLen += fileStatus.getLen();
                }
            }
            assertTrue(totalLen == 118);
        }

        conf.setClass(BspJob.VERTEX_RANGE_BALANCER_CLASS,
                      AutoBalancer.class,
                      VertexRangeBalancer.class);
        BspJob bspJob3 = new BspJob(conf, "testAutoBalancer");
        outputPath = new Path("/tmp/testAutoBalancer");
        hdfs.delete(outputPath, true);
        FileOutputFormat.setOutputPath(bspJob3, outputPath);
        assertTrue(bspJob3.run(true));
        if (getJobTracker() != null) {
            FileStatus [] fileStatusArr = hdfs.listStatus(outputPath);
            int totalLen = 0;
            for (FileStatus fileStatus : fileStatusArr) {
                if (fileStatus.getPath().toString().contains("/part-m-")) {
                    totalLen += fileStatus.getLen();
                }
            }
            assertTrue(totalLen == 118);
        }
    }
}