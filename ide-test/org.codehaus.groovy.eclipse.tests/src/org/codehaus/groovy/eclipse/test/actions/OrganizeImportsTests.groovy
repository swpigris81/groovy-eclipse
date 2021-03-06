/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.test.actions

import static org.eclipse.jdt.core.tests.util.GroovyUtils.isAtLeastGroovy
import static org.junit.Assume.assumeTrue

import org.junit.Ignore
import org.junit.Test

/**
 * Tests for {@link org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports}.
 */
final class OrganizeImportsTests extends OrganizeImportsTestSuite {

    @Test
    void testAddImport1() {
        String contents = '''
            FirstClass f
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport2() {
        String contents = '''
            def f = new FirstClass()
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport3() {
        String contents = '''
            def x(FirstClass f) { }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport4() {
        String contents = '''
            def x = { FirstClass f -> print '' }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport5() {
        String contents = '''
            class Main {
              FirstClass f
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport6() {
        String contents = '''
            class Main {
              FirstClass x() { }
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport7() {
        String contents = '''
            class Main {
              def x(FirstClass f) { }
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport8() {
        String contents = '''
            class Main {
              def x(FirstClass[] f) { }
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport9() {
        String contents = '''
            FirstClass[][] fs = []
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport10() {
        String contents = '''
            def fs = new FirstClass[0]
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport11() {
        String contents = '''
            def f = (FirstClass) null;
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport12() {
        String contents = '''
            def f = [:] as FirstClass
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddInnerImport1() {
        String contents = '''
            class Main {
              def x(Inner i) { }
            }
            '''
        doAddImportTest(contents, ['import other.Outer.Inner'])
    }

    @Test
    void testAddInnerImport2() {
        String contents = '''
            class Main {
              def x(Outer.Inner i) { }
            }
            '''
        doAddImportTest(contents, ['import other.Outer'])
    }

    @Test
    void testAddInnerImport3() {
        String contents = '''
            import other.Outer
            class Main {
              def x(Outer.Inner i) { }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testAddInnerImport4() {
        String contents = '''
            class Main {
              def x(UnknownTag t) { }
            }
            '''
        doAddImportTest(contents, ['import javax.swing.text.html.HTML.UnknownTag'])
    }

    @Test
    void testAddInnerImport5() {
        String contents = '''
            class Main {
              def x(HTML.UnknownTag t) { }
            }
            '''
        doAddImportTest(contents, ['import javax.swing.text.html.HTML'])
    }

    @Test // GRECLIPSE-470
    void testImportWithinMapLiteral() {
        String contents = '''
            import javax.util.concurrent.TimeUnit
            [value: TimeUnit.SECONDS]
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testAddImportWithinMapLiteral() {
        String contents = '''
            [value: TimeUnit.SECONDS]
            '''
        doAddImportTest(contents, ['javax.util.concurrent.TimeUnit'])
    }

    @Test
    void testAddImportForExtends() {
        String contents = '''
            class C extends FirstClass {
            }
            '''
        doAddImportTest(contents, ['other.FirstClass'])
    }

    @Test
    void testAddImportForImplements() {
        String contents = '''
            class C implements ConcurrentMap {
            }
            '''
        doAddImportTest(contents, ['import java.util.concurrent.ConcurrentMap'])
    }

    @Test
    void testAddImportForGenerics() {
        String originalContents = '''
            import java.util.Map.Entry

            Entry<SecondClass, HTML> entry
            '''
        String expectedContents = '''
            import java.util.Map.Entry

            import javax.swing.text.html.HTML

            import other.SecondClass

            Entry<SecondClass, HTML> entry
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1693
    void testAddImportForGenerics2() {
        createGroovyType 'p1', 'Foo', 'class Foo<T> {}'
        createGroovyType 'p2', 'Bar', 'class Bar {}'

        String originalContents = '''
            import p1.Foo
            class Baz extends Foo<Bar> {
            }
            '''
        String expectedContents = '''
            import p1.Foo
            import p2.Bar
            class Baz extends Foo<Bar> {
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testAddImportForGenerics3() {
        createGroovyType 'foo', 'Bar', 'class Bar {}'

        String contents = '''
            class Baz<T extends Bar> {
            }
            '''
        doAddImportTest(contents, ['import foo.Bar'])
    }

    @Test
    void testAddImportForGenerics4() {
        createGroovyType 'foo', 'Bar', 'class Bar {}'

        String contents = '''
            def maps = Collections.<Bar>emptyList();
            '''
        doAddImportTest(contents, ['import foo.Bar'])
    }

    @Test
    void testRemoveImport() {
        String originalContents = '''\
            import other.SecondClass
            '''
        String expectedContents = ''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport2() {
        String originalContents = '''\
            import other.SecondClass
            a
            '''
        String expectedContents = '''\
            a
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport3() {
        String originalContents = '''\
            import other.SecondClass
            other.SecondClass a
            '''
        String expectedContents = '''\
            other.SecondClass a
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport4() {
        String originalContents = '''\
            import other.SecondClass
            import javax.swing.text.html.HTML
            class Main {
              HTML f = null
            }
            '''
        String expectedContents = '''\
            import javax.swing.text.html.HTML
            class Main {
              HTML f = null
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport5() {
        String originalContents = '''\
            import other.ThirdClass
            import javax.swing.text.html.HTML
            import other.SecondClass
            class Main {
              HTML f = null
            }
            '''
        String expectedContents = '''\
            import javax.swing.text.html.HTML
            class Main {
              HTML f = null
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport6() {
        String originalContents = '''\
            import java.util.logging.Logger
            import groovy.util.logging.Log
            @Log
            class Main {
              void method() {
                log.info 'in method'
              }
            }
            '''
        String expectedContents = '''\
            import groovy.util.logging.Log
            @Log
            class Main {
              void method() {
                log.info 'in method'
              }
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRepeatImport() {
        String originalContents = '''\
            import javax.swing.text.html.HTML
            import javax.swing.text.html.HTML
            HTML.class
            '''
        String expectedContents = '''\
            import javax.swing.text.html.HTML
            HTML.class
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainImport() {
        String contents = '''\
            import javax.swing.text.html.HTML
            HTML.class
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport2() {
        String contents = '''\
            import java.lang.reflect.Method
            for (Method m : Object.class.getDeclaredMethods()) {
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport3() {
        String contents = '''\
            import java.lang.reflect.Method
            for (Method m in Object.class.getDeclaredMethods()) {
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport4() {
        // generics make the node length > "java.util.concurrent.Callable".length()
        String contents = '''\
            import java.util.concurrent.Callable
            class C implements Callable<java.util.List<java.lang.Object>> {
              List<Object> call() { }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport4a() {
        // multiple generics caused type to be perceived as "ConcurrentMap<java.util.regex.Pattern,"
        String contents = '''\
            import java.util.concurrent.ConcurrentMap
            ConcurrentMap<java.util.regex.Pattern, String> m
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport5() {
        String contents = '''\
            import java.util.regex.Pattern
            class C {
              @Lazy Pattern p = ~/123/
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport6() {
        String contents = '''\
            import java.util.regex.Pattern
            class C {
              @Lazy def p = Pattern.compile('123')
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport7() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            class C {
              @Lazy def p = compile('123')
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport8() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            class C {
              @groovy.transform.Memoized
              def meth() {
                compile('123')
              }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport9() {
        assumeTrue(isAtLeastGroovy(21))
        String contents = '''\
            import groovy.transform.AnnotationCollector
            import groovy.transform.EqualsAndHashCode
            import groovy.transform.ToString

            @AnnotationCollector([EqualsAndHashCode, ToString])
            public @interface Custom {
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport9a() {
        assumeTrue(isAtLeastGroovy(21))
        String contents = '''\
            import groovy.transform.AnnotationCollector
            import groovy.transform.EqualsAndHashCode
            import groovy.transform.ToString

            @ToString
            @EqualsAndHashCode
            @AnnotationCollector
            public @interface Custom {
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport10() {
        addJavaSource('interface I { static String NLS = "nls"; }', 'I', 'p')

        String contents = '''\
            import static p.I.NLS
            class Foo {
              @SuppressWarnings(NLS)
              def method() {
                System.getProperty('non.localized.string')
              }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testChoices() {
        String contents = '''
            FourthClass f = null
            '''
        doChoiceTest(contents, ['other2.FourthClass', 'other3.FourthClass', 'other4.FourthClass'])
    }

    @Test // GRECLIPSE-506
    void testImportDateFormat() {
        String contents = '''\
            import java.text.DateFormat
            new String(DateFormat.getDateInstance())
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-546
    void testImportDateFormat2() {
        String contents = '''\
            import java.text.DateFormat
            class Foo {
              Foo(DateFormat arg) { }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE 546
    void testImportDateFormat3() {
        String contents = '''
            class Foo {
              Foo(DateFormat arg) { }
            }
            '''
        doAddImportTest(contents, ['java.text.DateFormat'])
    }

    @Test // GRECLIPSE-643
    void testNoStackOverflowOnEnum() {
        String contents = '''\
            enum MyEnum {
              ONE_VALUE, ANOTHER_VALUE
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test @Ignore('Currently not possible due to heuristic in FindUnresolvedReferencesVisitor.handleVariable()')
    void testDynamicVariable() {
        String contents = '''
            HTML.NULL_ATTRIBUTE_VALUE
            '''
        doAddImportTest(contents, ['javax.swing.text.html.HTML'])
    }

    @Test
    void testDynamicVariable2() {
        String contents = '''\
            nothing.HTML.NULL_ATTRIBUTE_VALUE
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testDynamicVariable3() {
        String contents = '''
            new String(DateFormat.getDateInstance())
            '''
        doAddImportTest(contents, ['java.text.DateFormat'])
    }

    @Test // GRECLISPE-823
    void testThrownExceptions() {
        String originalContents = '''
            import java.util.zip.ZipException

            def x() throws BadLocationException {
            }
            def y() throws ZipException {
            }
            '''
        String expectedContents = '''
            import java.util.zip.ZipException

            import javax.swing.text.BadLocationException

            def x() throws BadLocationException {
            }
            def y() throws ZipException {
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-895
    void testCatchClausesExceptions() {
        String originalContents = '''
            import java.util.zip.ZipException

            try {
                nothing
            } catch (ZipException e1) {
            } catch (BadLocationException e2) {
            }
            '''
        String expectedContents = '''
            import java.util.zip.ZipException

            import javax.swing.text.BadLocationException

            try {
                nothing
            } catch (ZipException e1) {
            } catch (BadLocationException e2) {
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations1() {
        createGroovyType 'anns', 'Annotations', '''
            @interface NamedQueries {
              NamedQuery value();
            }

            @interface NamedQuery {
            }
            '''
        String contents = '''
            @NamedQueries(
              @NamedQuery
            )
            class MyEntity {  }
            '''
        doAddImportTest(contents, ['anns.NamedQueries', 'anns.NamedQuery'])
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations2() {
        createGroovyType 'anns', 'Annotations', '''
            @interface NamedQueries {
              NamedQuery value();
            }

            @interface NamedQuery {
            }'''
        String contents = '''\
            import anns.NamedQueries
            import anns.NamedQuery

            @NamedQueries(
              @NamedQuery
            )
            class MyEntity {  }
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations3() {
        createGroovyType 'anns', 'Annotations', '''
            @interface NamedQueries {
              NamedQuery[] value();
            }
            @interface NamedQuery {
            }
            '''

        String contents = '''
            @NamedQueries(
              [@NamedQuery]
            )
            class MyEntity {  }
            '''
        doAddImportTest(contents, ['anns.NamedQueries', 'anns.NamedQuery'])
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations4() {
        createGroovyType 'anns', 'Annotations', '''
            @interface NamedQueries {
              NamedQuery[] value();
            }
            @interface NamedQuery {
            }
            '''
        String contents = '''\
            import anns.NamedQueries
            import anns.NamedQuery

            @NamedQueries(
              [@NamedQuery]
            )
            class MyEntity {  }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass1() {
        createGroovyType 'inner', 'HasInner', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''
            InnerInner f
            '''
        doAddImportTest(contents, ['inner.HasInner.InnerInner'])
    }

    @Test
    void testInnerClass2() {
        createGroovyType 'inner', 'HasInner', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''\
            import inner.HasInner.InnerInner
            InnerInner f
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass3() {
        createGroovyType 'inner', 'HasInner', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''
            HasInner.InnerInner f
            '''
        doAddImportTest(contents, ['inner.HasInner'])
    }

    @Test
    void testInnerClass4() {
        createGroovyType 'inner', 'HasInner', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''\
            import inner.HasInner
            HasInner.InnerInner f
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass5() {
        createGroovyType 'a.b.c.d', 'E', '''
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E
            E x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass5a() {
        createGroovyType 'a.b.c.d', 'E', '''
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E.F
            F x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass5b() {
        createGroovyType 'a.b.c.d', 'E', '''
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E.F.G
            G x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass5c() {
        createGroovyType 'a.b.c.d', 'E', '''
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import static a.b.c.d.E.F.G.H
            def x = H
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport() {
        String contents = '''\
            import static java.lang.String.format
            format
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImportX() {
        String originalContents = '''\
            import static java.lang.String.format
            formage
            '''
        String expectedContents = '''\
            formage
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStaticImport2() {
        String contents = '''\
            import static java.lang.String.format
            format('Some %d', 1)
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport3() {
        String contents = '''\
            import static java.lang.String.format
            def formatter = { format 'blah', 42 }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport4() {
        String contents = '''\
            import static java.lang.Math.PI
            def area = n * PI
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport4a() {
        String contents = '''\
            import static java.lang.Math.PI
            def nan = PI.isNaN()
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport5() {
        String contents = '''\
            import static java.lang.Math.PI
            def area = compute(n, PI)
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport5a() {
        String originalContents = '''\
            import static java.lang.Math.PI
            def area = compute(n, Math.PI)
            '''
        String expectedContents = '''\
            def area = compute(n, Math.PI)
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStaticImport6() {
        String contents = '''\
            import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY
            @SuppressWarnings(value=EMPTY_STRING_ARRAY)
            def area = compute(n, PI)
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport7() {
        String contents = '''\
            import static java.util.Collections.emptyList
            class C {
              def method() {
                List list = emptyList();
              }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport7a() {
        String contents = '''\
            import static java.util.Collections.emptyList
            @groovy.transform.CompileStatic
            class C {
              def method() {
                List list = emptyList();
              }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport7b() {
        String contents = '''\
            import static java.util.Collections.emptyList
            @groovy.transform.TypeChecked
            class C {
              def method() {
                List list = emptyList();
              }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport8() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { String F = 'G.H' }
            '''
        String contents = '''\
            import static a.b.c.d.E.F
            class Foo {
              @SuppressWarnings(F)
              def one() {}
              @SuppressWarnings(F)
              def two() {}
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRepeatStaticImport() {
        String originalContents = '''\
            import static java.util.Collections.emptyList
            import static java.util.Collections.emptyList
            List list = emptyList();
            '''
        String expectedContents = '''\
            import static java.util.Collections.emptyList
            List list = emptyList();
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-929
    void testStarImport() {
        String contents = '''\
            import javax.swing.text.html.*
            HTML
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-929
    void testStarImport2() {
        // never remove star imports
        String contents = '''\
            import javax.swing.text.html.*
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStarImport3() {
        String originalContents = '''\
            import java.util.regex.Pattern
            import java.util.regex.*
            Pattern p = ~/123/
            '''
        String expectedContents = '''\
            import java.util.regex.*
            Pattern p = ~/123/
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-929
    void testStaticStarImport() {
        // never remove static star imports
        String contents = '''\
            import static java.lang.String.*
            format
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-929
    void testStaticStarImport2() {
        // never remove static star imports
        String contents = '''\
            import static java.lang.String.*
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticStarImport3() {
        String originalContents = '''\
            import static java.util.Collections.emptyList
            import static java.util.Collections.*
            List l = emptyList()
            '''
        String expectedContents = '''\
            import static java.util.Collections.*
            List l = emptyList()
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1219
    void testAnnotationsOnImports1() {
        String contents = '''\
            @Deprecated
            import javax.swing.text.html.*
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-1692
    void testFieldAnnotationImport() {
        String contents = '''\
            import groovy.transform.Field
            @Field
            def x = 0
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testCompileDynamicImport() {
        assumeTrue(isAtLeastGroovy(21))
        String contents = '''
            @CompileDynamic
            public void x() {
            }
            '''
        doAddImportTest(contents, ['groovy.transform.CompileDynamic'])
    }

    @Test // GRECLIPSE-1794
    void testCompileDynamicImport2() {
        assumeTrue(isAtLeastGroovy(21))
        String contents = '''\
            import groovy.transform.CompileDynamic
            @CompileDynamic
            public void x() {
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-1794
    void testCompileDynamicImport3() {
        assumeTrue(isAtLeastGroovy(21))
        String originalContents = '''\
            import groovy.transform.CompileDynamic
            import groovy.transform.CompileStatic
            @CompileDynamic
            public void x() {
            }
            '''
        String expectedContents = '''\
            import groovy.transform.CompileDynamic
            @CompileDynamic
            public void x() {
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1219
    void testAnnotationsOnImports2() {
        String originalContents = '''\
            @Deprecated
            import javax.swing.text.html.HTML
            '''
        String expectedContents = ''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1219
    void testAnnotationsOnImports3() {
        String contents = '''\
            @Deprecated
            import javax.swing.text.html.*
            HTML
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testDefaultImport0() {
        String contents = '''\
        interface Pattern {} // this should shadow java.util.regex.Pattern, et al.
        Pattern p = null
        '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport1() {
        // test a simple default import is removed
        String originalContents = '''\
            import java.util.List
            import groovy.util.Proxy
            List
            Proxy
            '''
        String expectedContents = '''\
            List
            Proxy
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport2() {
        // test that star default imports are removed
        String originalContents = '''\
            import java.util.*
            import groovy.util.*
            List
            Proxy
            '''
        String expectedContents = '''\
            List
            Proxy
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport3() {
        // test that BigInteger and BigDecimal are removed
        String originalContents = '''\
            import java.math.BigDecimal
            import java.math.BigInteger
            BigDecimal
            BigInteger
            '''
        String expectedContents = '''\
            BigDecimal
            BigInteger
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport4() {
        // test that aliased default import not removed
        String originalContents = '''\
            import java.util.List as LL
            import groovy.util.Proxy as PP
            LL
            PP
            '''
        String expectedContents = '''\
            import java.util.List as LL

            import groovy.util.Proxy as PP
            LL
            PP
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport5() {
        // test that static import whose container is default is not removed
        String contents = '''\
            import static java.util.Collections.swap
            swap
            '''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-1553
    void testCompileStaticAndMapStyleConstructor() {
        assumeTrue(isAtLeastGroovy(20))

        createGroovyType 'example2', 'Bar', '''
            class Bar {
                String name
            }'''

        String contents = '''
            import groovy.transform.CompileStatic

            import example2.Bar

            @CompileStatic
            class Foo {
              void apply() {
                  new Bar([name: 'test'])
              }
            }'''

        doContentsCompareTest(contents)
    }

    @Test
    void testReorderExistingImports() {
        // I've seen the static import get wiped out because it's not in the right position
        String originalContents = '''\
            import java.util.List
            import org.w3c.dom.Node
            import static java.util.Collections.emptyList
            import java.util.regex.Pattern

            Collection c = emptyList()
            Pattern p = ~/abc/
            Node n = null
            '''
        String expectedContents = '''\
            import static java.util.Collections.emptyList

            import java.util.regex.Pattern

            import org.w3c.dom.Node

            Collection c = emptyList()
            Pattern p = ~/abc/
            Node n = null
            '''

        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testOrganizeOnInvalidSource() {
        String contents = '''\
            import java.util.regex.Matcher
            import java.util.regex.Pattern

            class C {
              Pattern pattern = ~/123/
              Matcher matcher(String string) {
                def x /*=*/ pattern.matcher(string)
                return x
              }
            }
            '''

        doContentsCompareTest(contents)
    }

    @Ignore @Test
    void testOrganizeWithInterleavedComments() {
        String originalContents = '''\
            import java.util.regex.Pattern
            // blah blah blah blah blah...
            import java.util.regex.Matcher

            class C {
              Pattern pattern = ~/123/
              Matcher matcher(String string) {
                def x = pattern.matcher(string)
                return x
              }
            }
            '''
        String expectedContents = '''\
            // blah blah blah blah blah...
            import java.util.regex.Matcher
            import java.util.regex.Pattern

            class C {
              Pattern pattern = ~/123/
              Matcher matcher(String string) {
                def x = pattern.matcher(string)
                return x
              }
            }
            '''

        doContentsCompareTest(originalContents, expectedContents)
    }

    // TODO: Ensure imports come below header comment for class in the default package.
}
