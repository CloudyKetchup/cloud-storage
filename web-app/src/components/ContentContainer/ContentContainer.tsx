import React, { Component } from 'react';

import { EmptyContentBanner }	from '../EmptyContentBanner/EmptyContentBanner';
import { FileEntity } 			from '../../model/entity/FileEntity';
import { FolderEntity } 		from '../../model/entity/FolderEntity';
import App, { ContentContext }	from '../../App';
import File 					from '../File/File';
import Folder 					from '../Folder/Folder';
import { Link }					from "react-router-dom";
import LazyLoad					from 'react-lazyload';

type ContextMenuProps = {
	parent 	: App,
	action 	: (action: string) => void
	style 	: {
		top : string,
		left: string
	}
};

const ContextMenu = (props : ContextMenuProps) => (
	<div
		className="context-menu"
		id="default-context-menu"
		style={{
			top : props.style.top,
			left : props.style.left
		}}
	>
		<div onClick={() => props.action("upload-files")}>
			<div className="context-menu-icon">
				<i className="fas fa-file-upload"/>
			</div>
			<span>Upload files</span>
		</div>
		<Link to="/folder/create">
			<div className="context-menu-icon">
				<i className="fas fa-folder-plus"/>
			</div>
			<span>Create folder</span>
		</Link>
		<div onClick={() => props.action("delete-all")}>
			<div className="context-menu-icon">
				<i className="far fa-trash-alt"/>
			</div>
			<span>Delete All</span>
		</div>
		{
			props.parent.state.bufferElement
			&&
			<div onClick={() => props.action("paste")}>
				<div className="context-menu-icon">
					<i className="fas fa-clipboard"/>
				</div>
				<span>Paste</span>
			</div>
		}
	</div>
);

type ContentContainerState = {
	disableContextMenu 	: boolean,
	contextMenuParent 	: { id : string } | null,
	contextMenuShow		: boolean,
	contextMenuStyle	: {
		top : string,
		left: string
	}
};

export default class ContentContainer extends Component<{ folderId : string, parent : App }> {
	state : ContentContainerState = {
		disableContextMenu 	: false,
		contextMenuParent	: null,
		contextMenuShow 	: false,
		contextMenuStyle 	: {
			top  : '',
			left : ''
		}
	};

	componentDidMount() {
		const div = document.getElementById('content-container');

		if (div) {
			div.addEventListener('contextmenu', e => {
				e.preventDefault();

				this.setState({
					contextMenuShow		: true,
					contextMenuParent	: { id : 'content-container' },
					contextMenuStyle	: {
						top: e.y - 20,
						left: e.x - 73
					}
				});
			});
		}
		window.addEventListener('click', () => {
			const contextMenu = document.getElementById("default-context-menu");

			if (contextMenu && contextMenu.style) {
				contextMenu.style.marginLeft = "75px";
				contextMenu.style.opacity = "0";
			}
			setTimeout(() => this.setState({ contextMenuShow : false }), 100);
		});
	}

	createFile = (data: FileEntity) => {
		const mainParent = this.props.parent;

		return (
			<LazyLoad
				key={data.id}
				offset={30}
				overflow
			>
				<File
					key={data.path}
					data={data}
					mainParent={mainParent}
					container={this}
					handleAction={async (action: string) => {
						mainParent.setState({ elementSelected: data });

						await mainParent.handleContextMenuAction(action, data);
					}}
				/>
			</LazyLoad>
		);
	};

	createFolder = (data: FolderEntity) => {
		const mainParent = this.props.parent;

		return (
			<LazyLoad
				key={data.id}
				offset={30}
				overflow
			>
				<Folder
					key={data.path}
					data={data}
					mainParent={mainParent}
					container={this}
					handleAction={(action: string) => mainParent.handleContextMenuAction(action, data)}
					whenClicked={async () => {
						if (mainParent.state.elementSelected !== undefined
							&&
							mainParent.state.elementSelected.id === data.id
						) {
							await mainParent.updateFolder(data.id);
						} else {
							mainParent.setState({ elementSelected: data });
						}
					}}/>
			</LazyLoad>
		);
	};

	handleContextMenu = async (action: string) => {
		switch (action) {
			case 'upload-files':
				const input = document.getElementById("select-upload-files");

				if (input) input.click();
				break;
			case 'paste':
				await this.props.parent.pasteEntity(this.props.parent.state.bufferElement);
				break;
			case 'delete-all':
				await this.props.parent.deleteAllInFolder();
				break;
			default: break;
		}
	};

	separatorStyle = {
		width 		: '100px',
		height		: '1px',
		marginTop	: '10px',
		marginLeft	: '-20px',
		background	: 'gray'
	};

	render = () => (
		<ContentContext.Consumer>
			{content => 
				<div id="content-container">
					{this.props.children}
					{
						content.folders.length < 1
						&&
						content.files.length < 1
						&&
						<EmptyContentBanner/>
					}
					<div className="elements">
						{
							content.folders.length > 0
							&&
							<div className="content-elements-indicator">
								<span>Folders</span>
								<div style={this.separatorStyle}/>
							</div>
						}
						<div className="content-folders">
							{content.folders.map(this.createFolder)}
						</div>
						{
							content.files.length > 0
							&&
							<div className="content-elements-indicator">
								<span>Files</span>
								<div style={this.separatorStyle}/>
							</div>
						}
						<div className="content-files">{content.files.map(this.createFile)}</div>
					</div>
					{
						this.state.contextMenuShow
						&&
						!this.state.disableContextMenu
						&&
						<ContextMenu
							style={this.state.contextMenuStyle}
							parent={this.props.parent}
							action={this.handleContextMenu}
						/>
					}
				</div>
			}
		</ContentContext.Consumer>
	);
}
