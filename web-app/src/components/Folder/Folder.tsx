import React from 'react';

import { FolderEntity } from '../../model/entity/FolderEntity';
import EntityComponent, {EntityProps, EntityState} from '../EntityComponent/EntityComponent';

const contextMenuListener = async (e: MouseEvent, obj: Folder) => {
	e.preventDefault();

	obj.setState({
		contextMenuShow : true,
		contextMenuStyle : {
			top : `${e.clientY - 20}`,
			left : `${e.clientX - 73}`
		}
	});

	window.addEventListener('click', () => {
		const contextMenu = document.getElementById(`entity-${obj.props.data.id}-context-menu`);

		if (contextMenu && contextMenu.style) {
			contextMenu.style.marginLeft = "75px";
			contextMenu.style.opacity = "0";
		}
		setTimeout(() => windowClickListener(obj), 100);
	});

	obj.props.container.setState({ disableContextMenu : true });
};

const windowClickListener = async (obj: Folder) => {
	obj.setState({ contextMenuShow : false });

	obj.props.container.setState({ disableContextMenu : false });
};

interface FolderProps extends EntityProps {
	whenClicked: () => void
	data: FolderEntity
}

export default class Folder extends EntityComponent<FolderProps, EntityState> {

	componentDidMount = () => {
		const div = document.getElementById(`folder-${this.props.data.id}`);
			
		if (div !== null ) div.addEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

	componentWillUnmount = () => {
		const div = document.getElementById(`folder-${this.props.data.id}`);

		if (div !== null) div.removeEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

	render = () => (
		<div
			className="entity"
			key={this.props.data.path}
			id={`folder-${this.props.data.id}`}
		>
			{this.contextMenu(this.props.data, this.props.handleAction, this.props.mainParent)}
			<div onClick={this.props.whenClicked}>
				<div className="folder-icon">
					<i className="fas fa-folder"/>
				</div>
				<div className="entity-name">
					<span>{this.name(this.props.data.name)}</span>
				</div>
			</div>
		</div>
	);
}
