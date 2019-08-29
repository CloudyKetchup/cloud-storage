import React, {Component} from 'react';

import ContextMenu 		from '../contextmenu/ContextMenu'
import App 				from '../../../../App';
import {FileEntity} 	from '../../../../model/entity/FileEntity';
import ContentContainer from '../../container/ContentContainer';

const contextMenuListener = async (e: MouseEvent, obj: File) => {
	e.preventDefault();
	obj.setState({ contextMenuShow : true });

	obj.props.parent.setState({ disableContextMenu : true });

	window.addEventListener('click', () => windowClickListener(obj), false);
};

const windowClickListener = async (obj: File) => {
	obj.setState({ contextMenuShow : false });

	obj.props.parent.setState({ disableContextMenu : false });
};

type FileProps = {
	parent: ContentContainer
	mainParent: App
	data: FileEntity
	handleAction: (action: string) => void
};

export default class File extends Component<FileProps> {
	state = { contextMenuShow : false };

	componentDidMount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.addEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

	componentWillUnmount = () => {
		const div = document.getElementById(`file-${this.props.data.id}`);

		if (div !== null) div.removeEventListener('contextmenu', e => contextMenuListener(e, this), false);
	};

	contextMenu = () => {
		if (this.state.contextMenuShow) {
			return 	<ContextMenu
					parent={this.props.data}
					action={this.props.handleAction}
					onStart={() => this.props.mainParent.setState({ elementSelected : this.props.data })}
					/>
		}
	};

	name = () => {
		const name = this.props.data.name;

		return name.length > 18 ? `${name.substring(0, 17)}...` : name;
	};

	render = () => (
		<div
			className="entity"
			key={this.props.data.path}
			id={`file-${this.props.data.id}`}
		>
			{this.contextMenu()}
			<div className="entity-icon">
				<i className="fas fa-file"/>
			</div>
			<div className="entity-name">
				<span>{this.name()}</span>
			</div>
		</div>
	);
}