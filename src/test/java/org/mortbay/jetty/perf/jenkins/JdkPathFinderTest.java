package org.mortbay.jetty.perf.jenkins;

import hudson.ExtensionList;
import hudson.model.JDK;
import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.tools.ToolLocationNodeProperty;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JdkPathFinderTest
{
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testScriptedPipeline() throws Exception
    {
        String agentLabel = "my-agent";
        DumbSlave dumbSlave = jenkins.createOnlineSlave(Label.get(agentLabel));
        dumbSlave.getNodeProperties()
            .add(new ToolLocationNodeProperty(
                new ToolLocationNodeProperty.ToolLocation(ExtensionList.lookupSingleton(JDK.DescriptorImpl.class), "jdk11", "/home/foo/jdk11"),
                new ToolLocationNodeProperty.ToolLocation(ExtensionList.lookupSingleton(JDK.DescriptorImpl.class), "jdk16", "/home/foo/jdk16")
                ));

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n" +
                        "  jdkpathfinder nodes:['my-agent'], jdkNames: ['jdk11', 'jdk16']\n" +
                        "  sh 'cat my-agent-jdk-paths.properties'\n" +
                        "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains("jdk11=/home/foo/jdk11", completedBuild);
        jenkins.assertLogContains("jdk16=/home/foo/jdk16", completedBuild);
    }

}