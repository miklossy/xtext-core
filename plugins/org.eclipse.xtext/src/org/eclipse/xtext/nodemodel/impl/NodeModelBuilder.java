/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.nodemodel.impl;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class NodeModelBuilder {

	public void addChild(ICompositeNode node, AbstractNode child) {
		CompositeNode composite = (CompositeNode) node;
		if (composite.basicGetFirstChild() == null) {
			checkValidNewChild(child);
			composite.basicSetFirstChild(child);
			initializeFirstChildInvariant(composite, child);
		} else {
			addPrevious(composite.basicGetFirstChild(), child);
		}
	}
	
	public void associateWithSemanticElement(ICompositeNode node, EObject astElement) {
		CompositeNodeWithSemanticElement casted = (CompositeNodeWithSemanticElement) node;
		astElement.eAdapters().add(casted);
	}
	
	public ICompositeNode newCompositeNodeAsParentOf(EObject grammarElement, int lookahead, ICompositeNode existing) {
		CompositeNodeWithSemanticElement newComposite = new CompositeNodeWithSemanticElement();
		AbstractNode castedExisting = (AbstractNode) existing;
		newComposite.basicSetGrammarElement(grammarElement);
		newComposite.setLookAhead(lookahead);
		newComposite.basicSetParent(castedExisting.basicGetParent());
		if (newComposite.basicGetParent().basicGetFirstChild() == castedExisting) {
			newComposite.basicGetParent().basicSetFirstChild(newComposite);
		}
		if (castedExisting.basicGetNextSibling() == castedExisting) {
			newComposite.basicSetNextSibling(newComposite);
			newComposite.basicSetPreviousSibling(newComposite);
		} else {
			newComposite.basicSetNextSibling(castedExisting.basicGetNextSibling());
			newComposite.basicGetNextSibling().basicSetPreviousSibling(newComposite);
			newComposite.basicSetPreviousSibling(castedExisting.basicGetPreviousSibling());
			newComposite.basicGetPreviousSibling().basicSetNextSibling(newComposite);
		}
		newComposite.basicSetFirstChild(castedExisting);
		castedExisting.basicSetParent(newComposite);
		castedExisting.basicSetNextSibling(castedExisting);
		castedExisting.basicSetPreviousSibling(castedExisting);
		return compressAndReturnParent(existing);
	}

	protected void initializeFirstChildInvariant(CompositeNode node, AbstractNode child) {
		child.basicSetParent(node);
		child.basicSetNextSibling(child);
		child.basicSetPreviousSibling(child);
	}

	protected void checkValidNewChild(AbstractNode child) {
		if (child == null)
			throw new IllegalArgumentException("child may not be null");
		if (child.basicGetNextSibling() != null || child.basicGetPreviousSibling() != null)
			throw new IllegalStateException("prev has already a next or prev");
	}
	
	public void addPrevious(AbstractNode node, AbstractNode prev) {
		checkValidNewChild(prev);
		prev.basicSetPreviousSibling(node.basicGetPreviousSibling());
		prev.basicSetParent(node.basicGetParent());
		prev.basicSetNextSibling(node);
		if (node.basicGetPreviousSibling() != null) {
			node.basicGetPreviousSibling().basicSetNextSibling(prev);
		}
		node.basicSetPreviousSibling(prev);
	}
	
	public void addNext(AbstractNode node, AbstractNode next) {
		checkValidNewChild(next);
		next.basicSetNextSibling(node.basicGetNextSibling());
		next.basicSetParent(node.basicGetParent());
		next.basicSetNextSibling(node);
		if (node.basicGetNextSibling() != null) {
			node.basicGetNextSibling().basicSetNextSibling(next);
		}
		node.basicSetNextSibling(next);
	}
	
	public ICompositeNode newCompositeNode(EObject grammarElement, int lookahead, ICompositeNode parent) {
		CompositeNodeWithSemanticElement result = new CompositeNodeWithSemanticElement();
		result.basicSetGrammarElement(grammarElement);
		result.setLookAhead(lookahead);
		if (parent != null)
			addChild(parent, result);
		return result;
	}
	
	public ICompositeNode newRootNode(String input) {
		RootNode result = new RootNode();
		result.basicSetCompleteContent(input);
		return result;
	}

	public ILeafNode newLeafNode(int offset, int length, EObject grammarElement, boolean isHidden, String errorMessage,
			ICompositeNode parent) {
		LeafNode result = null;
		if (errorMessage != null) {
			if (isHidden) {
				result = new HiddenLeafNodeWithSyntaxError();
				((HiddenLeafNodeWithSyntaxError)result).basicSetSyntaxErrorMessage(new SyntaxErrorMessage(errorMessage, null));
			} else {
				result = new LeafNodeWithSyntaxError();
				((LeafNodeWithSyntaxError)result).basicSetSyntaxErrorMessage(new SyntaxErrorMessage(errorMessage, null));
			}
		} else {
			if (isHidden) {
				result = new HiddenLeafNode();
			} else {
				result = new LeafNode();
			}
		}
		result.basicSetGrammarElement(grammarElement);
		result.basicSetTotalOffset(offset);
		result.basicSetTotalLength(length);
		addChild(parent, result);
		return result;
	}
	
	public ICompositeNode compressAndReturnParent(ICompositeNode compositeNode) {
		CompositeNode casted = (CompositeNode) compositeNode;
		if (casted.basicGetSemanticElement() == null) {
			if (compositeNode instanceof CompositeNodeWithSemanticElement) {
				if (casted.getSyntaxErrorMessage() == null) {
					CompositeNode compressed = new CompositeNode();
					compressed.basicSetGrammarElement(casted.basicGetGrammarElement());
					compressed.setLookAhead(compositeNode.getLookAhead());
					replace(casted, compressed);
					return compressed.basicGetParent();
				} else {
					CompositeNodeWithSyntaxError compressed = new CompositeNodeWithSyntaxError();
					compressed.basicSetGrammarElement(casted.basicGetGrammarElement());
					compressed.setLookAhead(compositeNode.getLookAhead());
					compressed.basicSetSyntaxErrorMessage(casted.getSyntaxErrorMessage());
					replace(casted, compressed);
					return compressed.basicGetParent();
				}
			}
		}
		return casted.basicGetParent();
	}
	
	public INode setSyntaxError(INode node, SyntaxErrorMessage errorMessage) {
		if (node instanceof LeafNode) {
			LeafNode oldNode = (LeafNode) node;
			LeafNode newNode = null;
			if (oldNode.isHidden()) {
				HiddenLeafNodeWithSyntaxError newLeaf = new HiddenLeafNodeWithSyntaxError();
				newLeaf.basicSetSyntaxErrorMessage(errorMessage);
				newNode = newLeaf;
			} else {
				LeafNodeWithSyntaxError newLeaf = new LeafNodeWithSyntaxError();
				newLeaf.basicSetSyntaxErrorMessage(errorMessage);
				newNode = newLeaf;
			}
			newNode.basicSetTotalLength(oldNode.getTotalLength());
			newNode.basicSetTotalOffset(oldNode.getTotalOffset());
			newNode.basicSetGrammarElement(oldNode.basicGetGrammarElement());
			replace(oldNode, newNode);
			return newNode;
		} else {
			CompositeNode oldNode = (CompositeNode) node;
			CompositeNode newNode = null;
			if (oldNode.basicGetSemanticElement() != null) {
				CompositeNodeWithSemanticElementAndSyntaxError newComposite = new CompositeNodeWithSemanticElementAndSyntaxError();
				newComposite.basicSetSemanticElement(oldNode.basicGetSemanticElement());
				newComposite.basicSetSyntaxErrorMessage(errorMessage);
				oldNode.basicGetSemanticElement().eAdapters().remove(oldNode);
				newComposite.basicGetSemanticElement().eAdapters().add(newComposite);
				newNode = newComposite;
			} else {
				CompositeNodeWithSyntaxError newComposite = new CompositeNodeWithSyntaxError();
				newComposite.basicSetSyntaxErrorMessage(errorMessage);
				newNode = newComposite;
			}
			newNode.basicSetGrammarElement(oldNode.basicGetGrammarElement());
			newNode.setLookAhead(oldNode.getLookAhead());
			replace(oldNode, newNode);
			return newNode;
		}
	}

	protected void replace(AbstractNode oldNode, AbstractNode newNode) {
		newNode.basicSetParent(oldNode.basicGetParent());
		if ((oldNode.basicGetParent()).basicGetFirstChild() == oldNode) {
			(oldNode.basicGetParent()).basicSetFirstChild(newNode);
		}
		if (oldNode.basicGetNextSibling() == oldNode) {
			newNode.basicSetNextSibling(newNode);
		} else {
			newNode.basicSetNextSibling(oldNode.basicGetNextSibling());
			newNode.basicGetNextSibling().basicSetPreviousSibling(newNode);
		}
		if (oldNode.getPreviousSibling() == oldNode) {
			newNode.basicSetPreviousSibling(newNode);
		} else {
			newNode.basicSetPreviousSibling(oldNode.basicGetPreviousSibling());
			newNode.basicGetPreviousSibling().basicSetNextSibling(newNode);
		}
		if (oldNode instanceof CompositeNode) {
			CompositeNode oldComposite = (CompositeNode) oldNode;
			CompositeNode newComposite = (CompositeNode) newNode;
			AbstractNode child = oldComposite.basicGetFirstChild();
			if (child != null) {
				newComposite.basicSetFirstChild(child);
				while(child.basicGetParent() != newComposite) {
					child.basicSetParent(newComposite);
					child = child.basicGetNextSibling();
				}
			}
		}
	}

}
