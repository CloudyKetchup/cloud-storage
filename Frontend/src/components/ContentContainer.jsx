import React, { Component } from 'react';

import RenameElementDialog  from './RenameElementDialog';
import CreateFolderDialog 	from './CreateFolderDialog';
import DefaultContextMenu	from './DefaultContextMenu';
import Folder 				from './Folder';
import File 				from './File';

export default class ContentContainer extends Component {
	state = {
		disableContextMenu : false,
		contextMenuShow : false,
		contextMenuStyle : {
			top : '',
			left : ''
		}
	};

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
	
	createFile = data => {
		const mainParent = this.props.parent;

		return (
			<File 
				key={data.path}
				data={data}
				mainParent={mainParent}
				parent={this}
				handleAction={action => {
					mainParent.setState({ elementSelected : data });

					mainParent.handleContextMenuAction(action, data);
				}}
			/>
		);
	}

	createFolder = data => {
		const mainParent = this.props.parent;

		return (
			<Folder
				key={data.path}
				data={data}
				mainParent={mainParent}
				parent={this}
				handleAction={action => mainParent.handleContextMenuAction(action, data)}
				whenClicked={() => 
					mainParent.state.elementSelected !== undefined
					&&
					mainParent.state.elementSelected.id === data.id
							? mainParent.updateFolderInfo(data.id)
							: mainParent.setState({ elementSelected : data})
				}/>
		);
	};

	customDialog() {
		if (this.props.parent.state.createFolderDialog) {
			return <CreateFolderDialog
						parent 	   = {this.props.parent}
						sendFolder = {folder => this.props.parent.sendNewFolder(folder)}
					/>
		}else if (this.props.parent.state.renameElementDialog) {
			return  <RenameElementDialog
						parent   = {this.props.parent}
						onRename = {newName => this.props.parent.sendRenameRequest(newName)}
					/>
		}
	}

	render() {
		return (
			<div id='content-container'>
				{this.customDialog()}
				{this.props.children}
				<div className="elements">
					{this.props.folders.map(folder => this.createFolder(folder))}
					{this.props.files.map(file => this.createFile(file))}
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