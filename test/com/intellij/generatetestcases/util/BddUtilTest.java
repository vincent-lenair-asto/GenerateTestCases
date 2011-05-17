package com.intellij.generatetestcases.util;


import com.intellij.generatetestcases.test.ExpectExceptionsExecutor;
import com.intellij.generatetestcases.test.ExpectExceptionsTemplate;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.testFramework.IdeaTestCase;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// important :)
//@RunWith(JUnit4.class)

/**
 * TODO find out how to extends Junit 3 test class and run it with Junit 4 annotatiosn
 * temporarily methods names has been changed to include the test word at the beginnig
 */
public class BddUtilTest extends IdeaTestCase {


//
//    @Before
//    @Override
//    public void setUp() throws Exception {
//        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
//    }
//
//    @After
//    @Override
//    public void tearDown() throws Exception {
//        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
//    }


    //
//    public static junit.framework.Test suite() {
//        return new JUnit4TestAdapter(BddUtilTest.class);
//    }
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        final File testRoot = new File(PluginPathManager.getPluginHomePath("GenerateTestCases") + "/testData", "eml");
//        assertTrue(testRoot.getAbsolutePath(), testRoot.isDirectory());
//
//        final File currentTestRoot = new File(testRoot, getTestName(true));
//        assertTrue(currentTestRoot.getAbsolutePath(), currentTestRoot.isDirectory());
//        FileUtil.copyDir(currentTestRoot, new File(getProject().getBaseDir().getPath()));
//    }

    /**
     * @verifies create a appropiate name for the test method
     * @see BddUtil#generateTestMethodNameForJUNIT4(String, String)
     */
    @Test
    public void testGenerateTestMethodName_shouldCreateAAppropiateNameForTheTestMethod()
            throws Exception {

        String methodName = "generateTestMethodName";
        String description = "create a appropiate name for the test method";
        String testMethodName = BddUtil.generateTestMethodNameForJUNIT4(methodName, description);
        assertEquals("generateTestMethodName_shouldCreateAAppropiateNameForTheTestMethod", testMethodName);

    }

    /**
     * @verifies fail if wrong args
     * @see BddUtil#generateTestMethodNameForJUNIT4(String, String)
     */
    @Test
    public void testGenerateTestMethodName_shouldFailIfWrongArgs() throws Exception {

        ExpectExceptionsExecutor.execute(new ExpectExceptionsTemplate() {
            public Class getExpectedException() {
                return IllegalArgumentException.class;
            }

            public void doInttemplate() {
                BddUtil.generateTestMethodNameForJUNIT4("", "");
            }
        });

        ExpectExceptionsExecutor.execute(new ExpectExceptionsTemplate() {
            public Class getExpectedException() {
                return IllegalArgumentException.class;
            }

            public void doInttemplate() {
                BddUtil.generateTestMethodNameForJUNIT4(null, null);
            }
        });


    }

    /**
     * @verifies return psi element pairs for start element and end element in each line for each should tag
     * @see BddUtil#getElementPairsInDocTag(com.intellij.psi.javadoc.PsiDocTag)
     */
    public void testGetElementPairsInDocTag_shouldReturnPsiElementPairsForStartElementAndEndElementInEachLineForEachShouldTag() throws Exception {

//        Case 1
        assertForOneShouldTag("/**\n" +
                "     * @should foo\n" +
                "     */", 0, new int[][]{{0, 2, 2}}, myProject);

        // case 2
        assertForOneShouldTag("/**\n" +
                "     * @should foo yoo zas\n" +
                "     */", 0, new int[][]{{0, 2, 4}}, myProject);

        // case 3
        assertForOneShouldTag("/**\n" +
                "     * @should foo bar zas\n" +
                "     * yoo doo right\n" +
                "     * asgasdg asdfgasd\n" +
                "     */", 0, new int[][]{{0, 2, 4}, {1, 7, 7}, {2, 10, 10}}, myProject);

        // matches pair caught for 'yoo' description
        assertForOneShouldTag("/**\n" +
                "     * @should doo  \n" +
                "     * @should yoo\n" +
                "     * \n" +
                "     */"
                , 1, new int[][]{{0, 2, 2}}, myProject);

    }

    /**
     * @param docCommentText
     * @param shouldTagIndex
     * @param matchings      { {line, startElIdx, endElIdx}, {line, startElIdx, endElIdx}}
     * @param project
     */
    private void assertForOneShouldTag(String docCommentText, int shouldTagIndex, int[][] matchings, Project project) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiDocComment case1DocComment = elementFactory.createDocCommentFromText(docCommentText, null);
        PsiDocTag[] docCommentTags = case1DocComment.getTags();
        PsiDocTag case1FirstTag = docCommentTags[shouldTagIndex];
        //  get doctag elements
        List<BddUtil.DocOffsetPair> case1Matches = BddUtil.getElementPairsInDocTag(case1FirstTag);
        for (int[] matching : matchings) {
            assertThat(case1Matches.get(matching[0]).getStart(), is(case1FirstTag.getChildren()[matching[1]]));
            assertThat(case1Matches.get(matching[0]).getEnd(), is(case1FirstTag.getChildren()[matching[2]]));
        }
    }

    /**
     * @verifies not consider part of the problem whitespace/nl for not ending tags
     * @see BddUtil#getElementPairsInDocTag(com.intellij.psi.javadoc.PsiDocTag)
     */
    public void testGetElementPairsInDocTag_shouldNotConsiderPartOfTheProblemWhitespacenlForNotEndingTags() throws Exception {

//        int[][] matchings = new int[][]{{0, 2, 2}};
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(myProject);
        //  count psiDocTags for first tag in javadoc and expect one only
        PsiDocComment docCommentFromText1 = elementFactory.createDocCommentFromText("/**\n" +
                "     * @should doo  \n" +
                "     * @should yoo\n" +
                "     * \n" +
                "     */", null);
        //  get doctag elements
        assertThat(BddUtil.getElementPairsInDocTag(docCommentFromText1.getTags()[0]).size(), is(1));

        PsiDocComment docCommentFromText2 = elementFactory.createDocCommentFromText("/**\n" +
                "     * @should doo zas \n" +
                "     * @should yoo\n" +
                "     * \n" +
                "     */", null);

        assertThat(BddUtil.getElementPairsInDocTag(docCommentFromText2.getTags()[0]).size(), is(1));
    }
}