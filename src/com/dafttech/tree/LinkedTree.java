package com.dafttech.tree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LinkedTree<E> implements Tree<E>, Cloneable, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6857447299766592010L;
    private Tree<E> root;
    private E leaf;
    private List<Tree<E>> branches = new LinkedList<Tree<E>>();

    public LinkedTree(Tree<E> root, E leaf, Collection<? extends Tree<E>> branches) {
        this.root = root;
        this.leaf = leaf;
        if (branches != null) this.branches.addAll(branches);
    }

    public LinkedTree(Tree<E> tree) {
        this(tree.getRoot(), tree.getLeaf(), tree.getBranches());
    }

    public LinkedTree(Tree<E> root, E leaf) {
        this(root, leaf, null);
    }

    public LinkedTree(E leaf) {
        this(null, leaf, null);
    }

    public LinkedTree() {
        this(null, null, null);
    }

    @Override
    public boolean hasLeaf() {
        return leaf != null;
    }

    @Override
    public E getLeaf() {
        return leaf;
    }

    @Override
    public void setLeaf(E leaf) {
        this.leaf = leaf;
    }

    @Override
    public boolean hasRoot() {
        return root != null;
    }

    @Override
    public Tree<E> getRoot() {
        return root;
    }

    @Override
    public boolean hasBranches() {
        return branches.size() > 0;
    }

    @Override
    public int size() {
        return branches.size();
    }

    @Override
    public List<Tree<E>> getBranches() {
        return branches;
    }

    @Override
    public Tree<E> getBranch(int index) {
        return branches.get(index);
    }

    @Override
    public void addBranch(E leaf) {
        branches.add(new LinkedTree<E>(this, leaf));
    }

    @Override
    public void addBranch(Tree<E> branch) {
        branches.add(branch);
    }

    @Override
    public void addBranch(int index, E leaf) {
        branches.add(index, new LinkedTree<E>(this, leaf));
    }

    @Override
    public void addBranch(int index, Tree<E> branch) {
        branches.add(index, branch);
    }

    @Override
    public void addBranchesByLeaves(Collection<E> leaves) {
        for (E leaf : leaves)
            branches.add(new LinkedTree<E>(leaf));
    }

    @Override
    public void addBranches(Collection<Tree<E>> branches) {
        for (Tree<E> branch : branches)
            branches.add(branch);
    }

    @Override
    public void removeBranch(int index) {
        branches.remove(index);
    }

    @Override
    public void removeBranch(Tree<E> branch) {
        branches.remove(branch);
    }

    @Override
    public void removeBranch(E leaf) {
        branches.remove(leaf);
    }

    @Override
    public int getTreeSize() {
        int size = 1;
        for (Tree<E> branch : branches)
            size += branch.getTreeSize();
        return size;
    }

    @Override
    public List<E> getTreeLeaves() {
        List<E> leaves = new LinkedList<E>();
        leaves.add(leaf);
        for (Tree<E> branch : branches)
            leaves.addAll(branch.getTreeLeaves());
        return leaves;
    }

    @Override
    public Object clone() {
        return new LinkedTree<E>(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj == leaf;
    }

    private void writeObject(ObjectOutputStream objectoutputstream) throws IOException {
        objectoutputstream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectinputstream) throws IOException, ClassNotFoundException {
        objectinputstream.defaultReadObject();
    }
}