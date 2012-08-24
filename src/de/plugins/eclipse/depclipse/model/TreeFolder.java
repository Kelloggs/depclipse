/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/

/**
 * Created on 31.12.2002
 */
package de.plugins.eclipse.depclipse.model;
import java.util.ArrayList;

import jdepend.framework.JavaPackage;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

public class TreeFolder extends TreeObject {
    protected boolean cycle;
    private ArrayList<TreeObject> children;
    private ArrayList<IJavaElement> iJavaElements;
    private final IJavaElement javaElement;

    public TreeFolder(IJavaElement javaElement) {
        super();
        this.javaElement = javaElement;
        children = new ArrayList<TreeObject>();
        iJavaElements = new ArrayList<IJavaElement>();
        addIJavaElement(javaElement);
        if(javaElement != null) {
        	try {
				setIResource(javaElement.getCorrespondingResource());
			} catch (JavaModelException e) {
				// just do nothing here
			}
        }
    }

    public TreeFolder() {
        this((IJavaElement)null);
    }

    public IJavaElement getIJavaElement(){
        return javaElement;
    }

    @SuppressWarnings("rawtypes")
	public Object getAdapter(Class key) {
        if(key == IJavaElement.class){
            return getIJavaElement();
        }
        if(key == IResource.class){
            return getIResource();
        }
        return null;
    }

    private boolean addIJavaElement(IJavaElement resource){
        if(resource == null){
            return false;
        }

        if(iJavaElements.contains(resource)){
            return false;
        }
        iJavaElements.add(resource);
        return true;
    }


    public void addChild(TreeObject child) {
        if(child == null){
            return;
        }
        if(!children.contains(child)){
            children.add(child);
            child.setParent(this);
        } else if(!child.isLeaf()){
            TreeFolder tf = (TreeFolder)children.get(children.indexOf(child));
            TreeObject [] tc = ((TreeFolder)child).getChildren();
            for (int i = 0; i < tc.length; i++) {
                tf.addChild(tc[i]);
            }
            IJavaElement [] elements = (IJavaElement [])((TreeFolder)child).getIJavaElements().toArray(new IJavaElement [0]);
            for (int i = 0; i < elements.length; i++) {
                tf.addIJavaElement(elements[i]);
            }
        }
    }

    public void removeChild(TreeObject child) {
        children.remove(child);
        if(child!= null){
            child.setParent(null);
        }
    }
    public TreeObject[] getChildren() {
        return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
    }
    public boolean hasChildren() {
        return children.size() > 0;
    }
    public boolean isLeaf() {
        return false;
    }

    public boolean hasCycle(){
        if(cycle) {
            return cycle;
        }
        if(children != null){
            TreeObject to;
            for (int i = 0; i < children.size(); i++) {
                to = (TreeObject)children.get(i);
                if(to.hasCycle()){
                    return true;
                }
            }
        }
        return cycle;
    }

    public TreeObject findChild(JavaPackage pack){
        if(children == null) {
            return null;
        }
        TreeObject to;
        for (int i = 0; i < children.size(); i++) {
            to = (TreeObject)children.get(i);
            if(to.getPackageName().endsWith(pack.getName())){
                return to;
            }
            if(!to.isLeaf()){
                to = ((TreeFolder)to).findChild(pack);
                if(to != null){
                    return to;
                }
            }
        }
        return null;
    }

    public ArrayList<String> getClassesLocation() throws JavaModelException {
        ArrayList<IJavaElement> myIJavaElements = getIJavaElements();
        String dir;
        ArrayList<String> dirs = new ArrayList<String>();
        for (int i = 0; i < myIJavaElements.size(); i++) {
            dir = getPackageOutputPath((IJavaElement) myIJavaElements.get(i));
            if(!dirs.contains(dir)){
                dirs.add(dir);
            }
        }
        if(dirs.size() == 0){
            if(getIResource() != null){
                dirs.add(getIResource().getLocation().toOSString());
            }
        }

        return dirs;
    }

    public ArrayList<IJavaElement> getIJavaElements(){
        return iJavaElements;
    }

    public IResource [] getIResources() throws JavaModelException{
        IJavaElement [] elements = (IJavaElement [])getIJavaElements().toArray(new IJavaElement [0]);
        ArrayList<IResource> resources = new ArrayList<IResource>();
        IResource tresource;
        for (int i = 0; i < elements.length; i++) {
            tresource = elements[i].getCorrespondingResource();
            if(tresource != null && !resources.contains(tresource)){
                resources.add(tresource);
            }
        }
        if(getIResource() != null){
            resources.add(getIResource());
        }
        return (IResource [])resources.toArray(new IResource [resources.size()]);
    }

    public void setContainsCycle(boolean cycle){
        this.cycle = cycle;
    }

    public String getName() {
        return getPackageName();
    }

    public String getPackageName() {
        ArrayList<IJavaElement> elements = this.getIJavaElements();
        if(elements.size() == 0){
            if(getIResource() != null){
                String path = iResource.getFullPath().removeFirstSegments(1).toString();
                return path.replace('/', '.');
            }
            return "";        //$NON-NLS-1$
        }
        return getJavaPackageName((IJavaElement)elements.get(0));
    }
}