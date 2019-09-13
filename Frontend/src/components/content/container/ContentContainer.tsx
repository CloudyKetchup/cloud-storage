import React, {Component} from 'react';

import DefaultContextMenu	from './contextmenu/DefaultContextMenu';
import Folder 				from '../elements/folder/Folder';
import File 				from '../elements/file/File';
import App					from '../../../App';
import {FileEntity} 		from '../../../model/entity/FileEntity';
import {FolderEntity} 		from '../../../model/entity/FolderEntity';
import {EmptyContentBanner} from './emptycontent/EmptyContentBanner';

type ContentContainerProps = {
	parent 	: App,
	files 	: FileEntity[],
	folders : FolderEntity[]
}

export default class ContentContainer extends Component<ContentContainerProps> {
	state = {
		disableContextMenu : false,
		contextMenuShow : false,
		contextMenuStyle : {
			top : '',
			left : ''
		}
	};

	componentDidMount() {
		const div = document.getElementById('content-container');

		if (div !== null) {
			div.addEventListener('contextmenu', e => {
				e.preventDefault();

				const rightPanel = document.getElementById("right-panel");

				if (rightPanel !== null) rightPanel.style.right = '-300px';

				this.setState({
					contextMenuShow: true,
					contextMenuStyle: {
						top: e.y - 70,
						left: e.x - 275
					}
				})
			});
		}

		window.addEventListener('click', () => this.setState({ contextMenuShow : false }), false);
	}

	createFile = (data: FileEntity) => {
		const mainParent = this.props.parent;

		return (
			<File
				key={data.path}
				data={data}
				mainParent={mainParent}
				parent={this}
				handleAction={(action: string) => {
					mainParent.setState({ elementSelected : data });

					mainParent.handleContextMenuAction(action, data);
				}}
			/>
		);
	};

	createFolder = (data: FolderEntity) => {
		const mainParent = this.props.parent;

		return (
			<Folder
				key={data.path}
				data={data}
				mainParent={mainParent}
				parent={this}
				handleAction={(action: string) => mainParent.handleContextMenuAction(action, data)}
				whenClicked={() => {
					if (mainParent.state.elementSelected !== undefined
						&&
						mainParent.state.elementSelected.id === data.id
				) {
						mainParent.updateFolderInfo(data.id);

						mainParent.addNavNode(data);
					} else {
						mainParent.setState({ elementSelected : data });
					}
				}}/>
		);
	};

	handleContextMenu = (action: string) => {
		switch (action) {
			case 'upload-files':
				const input = document.getElementById("select-upload-files");

				if (input !== null) input.click();
				break;
			case 'paste':
				this.props.parent.pasteEntity(this.props.parent.state.bufferElement);
				break;
			case 'delete-all':
				this.props.parent.sendDeleteAll();
				break;
			default: break;
		}
	};

	separatorStyle = {
		width : '100px',
		height: '1px',
		marginTop: '10px',
		marginLeft: '-20px',
		background: 'gray'
	};

	render = () => (
		<div id="content-container">
			{this.props.children}
			{this.props.folders.length < 1
					&&
					this.props.files.length < 1
					&&
					<EmptyContentBanner/>}
					<div className="elements">
						{this.props.folders.length > 0
							?	<div className="content-elements-indicator">
									<span>Folders</span>
									<div style={this.separatorStyle}/>
								</div>
						: undefined}
						<div className="content-folders">
							{this.props.folders.map(this.createFolder)}
						</div>
						{this.props.files.length > 0
							?	<div className="content-elements-indicator">
									<span>Files</span>
									<div style={this.separatorStyle}/>
								</div>
						: undefined}
						<div className="content-files">
							{this.props.files.map(this.createFile)}
						</div>
					</div>
					{this.state.contextMenuShow
						&&
						!this.state.disableContextMenu 
						&&
						<DefaultContextMenu
							style={this.state.contextMenuStyle}
							parent={this.props.parent}
							action={this.handleContextMenu}/>}
		</div>
	);
}
