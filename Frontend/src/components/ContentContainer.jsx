import React, { Component } from 'react';

import RenameElementDialog  from './RenameElementDialog';
import CreateFolderDialog 	from './CreateFolderDialog';
import DefaultContextMenu	from './DefaultContextMenu';
import Folder 				from './Folder';
import File 				from './File';

export default class ContentContainer extends Component {
	state={
		disableContextMenu : false,
		contextMenuShow : false,
		contextMenuStyle : {
			top : '',
			left : ''
		}
	}

	componentDidMount() {
		document.getElementById('content-container').addEventListener('contextmenu', e => {
			e.preventDefault();
			this.setState({ 
				contextMenuShow : true,
				contextMenuStyle : {
					top : e.y - 70,
					left : e.x - 275
				}
			})	
		});

		window.addEventListener('click', () => this.setState({ contextMenuShow : false }), false);
	}
	
	createElement(element) {
		const mainParent = this.props.parent;

		return element.type === 'FILE' 
			? <File 
				key={element.id}
				name={element.name}
				/>
			: <Folder
				data={element}
				key={element.id}
				mainParent={mainParent}
				parent={this}
				handleAction={action => this.props.parent.handleContextMenuAction(action, element)}
				whenClicked={() => 
					mainParent.state.elementSelected !== undefined
					&&
					mainParent.state.elementSelected.id === element.id
							? mainParent.updateElementsData(element.id)
							: mainParent.setState({ elementSelected : element})
				}/>
	}

	customDialog() {
		if (this.props.parent.state.createFolderDialog) {
			return <CreateFolderDialog
					parent = {this.props.parent}
					sendFolder  = {folder => this.props.parent.sendNewFolder(folder)}
					/>
		}else if (this.props.parent.state.renameElementDialog) {
			return  <RenameElementDialog
					parent  = {this.props.parent}
					onRename= {newName => this.props.parent.sendRenameRequest(newName)}
					/>
		}
	}

	render() {
		return (
			<div id='content-container'>
				{this.customDialog()}
				{this.props.children}
				<div className="elements">
					{this.props.elementsData.map(element => this.createElement(element))}
				</div>
				{this.state.contextMenuShow && !this.state.disableContextMenu
					? <DefaultContextMenu
						style={this.state.contextMenuStyle}
						parent= {this.props.parent}
						action= {action => this.props.parent.handleContextMenuAction(action)}
						/>
					: undefined}
			</div>
		);
	}
}