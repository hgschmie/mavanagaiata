/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.junit.Before;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.jgit.JGitRepository;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class MojoAbstractTest<T extends AbstractGitMojo> {

    protected File baseDir;

    protected T mojo;

    protected Properties projectProperties;

    protected GitRepository repository;

    protected void testError(String errorMessage) {
        try {
            this.mojo.repository = mock(JGitRepository.class, new Answer() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    throw new GitRepositoryException("");
                }
            });
            this.mojo.run();
            fail("No exception thrown.");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(MojoExecutionException.class)));
            assertThat(e.getMessage(), is(equalTo(errorMessage)));
            assertThat(e.getCause(), is(instanceOf(GitRepositoryException.class)));
        }
    }

    @Before
    public void setup() throws Exception {
        this.baseDir = mock(File.class);
        when(this.baseDir.exists()).thenReturn(true);

        MavenProject project   = mock(MavenProject.class);
        this.projectProperties = new Properties();
        when(project.getProperties()).thenReturn(this.projectProperties);

        this.repository = mock(GitRepository.class);

        @SuppressWarnings("unchecked")
        Class<T> mojoClass = ((Class<T>)((ParameterizedType) this.getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]);
        if (!Modifier.isAbstract(mojoClass.getModifiers())) {
            this.mojo = mojoClass.newInstance();
        }
        this.mojo.baseDir    = this.baseDir;
        this.mojo.project    = project;
        this.mojo.repository = this.repository;
    }

    protected void assertProperty(Object value, String key) {
        for(String prefix : this.mojo.propertyPrefixes) {
            assertThat(this.projectProperties.get(prefix + "." + key), is(equalTo(value)));
        }
    }

}
