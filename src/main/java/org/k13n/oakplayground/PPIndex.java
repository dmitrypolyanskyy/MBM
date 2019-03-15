package org.k13n.oakplayground;


import org.apache.jackrabbit.oak.api.ContentSession;
import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.api.Tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PPIndex {

    private Tree root;
    private String property;

    public PPIndex (Tree root, String property) {
        this.root = root;
        this.property = property;
    }

    public void insert (String value, String path) {
        Tree currentNode = root;
        if (currentNode.hasChild(value)){
            currentNode = currentNode.getChild(value);
        }
        else{
            currentNode = currentNode.addChild(value);
        }

        String delimiter = "/";
        String storedArray[] = path.split(delimiter);
        for (int i = 1; i < storedArray.length; i++){
            if (currentNode.hasChild(storedArray[i])){
                currentNode = currentNode.getChild(storedArray[i]);
            }
            else{
                currentNode = currentNode.addChild(storedArray[i]);
            }
        }
        currentNode.setProperty(property,value);
    }


    public void delete (String value, String path) {
        Tree currentNode = root;
        if (currentNode.hasChild(value)){
            currentNode = currentNode.getChild(value);
        }
        else{
            return;
        }

        String delimiter = "/";
        String storedArray[] = path.split(delimiter);
        for (int i = 1; i < storedArray.length; i++){
            if (currentNode.hasChild(storedArray[i])){
                currentNode = currentNode.getChild(storedArray[i]);
            }
            else{
                return;
            }
        }
        currentNode.removeProperty(property);
        while (currentNode.getChildrenCount(1) == 0 && !currentNode.hasProperty(property) && currentNode != root){
            Tree parentNode = currentNode.getParent();
            currentNode.remove();
            currentNode = parentNode;
        }


    }

    public List<String> search (String value, String path){
        String reg = path;
        ArrayList<String> result = new ArrayList<>();
        Tree currentNode = root;
        if (currentNode.hasChild(value)){
            currentNode = currentNode.getChild(value);
        }
        else{
            return result;
        }

        String delimiter = "/";
        String storedArray[] = path.split(delimiter);
        for (int i = 1; i < storedArray.length; i++){
            if (currentNode.hasChild(storedArray[i])){
                currentNode = currentNode.getChild(storedArray[i]);
            }
            else{
                return result;
            }
        }

        //String startPath[] = path.split(delimiter);

        LinkedList<Tree> queue = new LinkedList<Tree>();
        queue.add(currentNode);
        while (!queue.isEmpty()) {
            Tree node = queue.removeFirst();
            if (node.hasProperty(property)){
                result.add(node.getPath());
            }
            for (Tree child:node.getChildren()){
                queue.add(child);
            }
        }
        return result;
    }

}
