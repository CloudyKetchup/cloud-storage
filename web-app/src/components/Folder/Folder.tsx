import React from 'react';

import { FolderEntity } from '../../model/entity/FolderEntity';
import EntityComponent, { EntityProps } from '../EntityComponent/EntityComponent';

const contextMenuListener = async (e: MouseEvent, obj: Folder) => {
	e.preventDefault();
	obj.setState({
		contextMenuShow : true,
		contextMenuStyle : {
			top : e.clientY - 20,
			left : e.clientX - 73
		}
	});

	obj.props.parent.setState({ disableContextMenu : true });

	window.addEventListener('click', () => windowClickListener(obj), false);
};

const windowClickListener = async (obj: Folder) => {
	obj.setState({ contextMenuShow : false });

	obj.props.parent.setState({ disableContextMenu : false });
};

interface FolderProps extends EntityProps {
	whenClicked: () => void
	data: FolderEntity
}

export default class Folder extends EntityComponent<FolderProps> {

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
