import React, {Component} from 'react';

import ContextMenu 		from '../contextmenu/ContextMenu'
import ContentContainer from '../../container/ContentContainer';
import App 				from '../../../../App';
import {FolderEntity} 	from '../../../../model/entity/FolderEntity';

const contextMenuListener = async (e: MouseEvent, obj: Folder) => {
	e.preventDefault();
	obj.setState({ contextMenuShow : true });

	obj.props.parent.setState({ disableContextMenu : true });

	window.addEventListener('click', () => windowClickListener(obj), false);
}

const windowClickListener = async (obj: Folder) => {
	obj.setState({ contextMenuShow : false });

	obj.props.parent.setState({ disableContextMenu : false });
}

type FolderProps = {
	parent: ContentContainer
	mainParent: App
	data: FolderEntity
	handleAction: (action: string) => void
	whenClicked: () => void
};

export default class Folder extends Component<FolderProps> {
	state = { contextMenuShow : false };

	componentDidMount() {
		const div = document.getElementById(`folder-${this.props.data.id}`);
			
		if (div !== null )div.addEventListener('contextmenu', e => contextMenuListener(e, this), false);
	}

	componentWillUnmount() {
		const div = document.getElementById(`folder-${this.props.data.id}`);

		if (div !== null) div.removeEventListener('contextmenu', e => contextMenuListener(e, this), false);
	}

	contextMenu() {
		if (this.state.contextMenuShow) {
			return 	<ContextMenu
					parent={this.props.data}
					action={action => this.props.handleAction(action)}
					onStart={() => this.props.mainParent.setState({ elementSelected : this.props.data })}
					/>
		}
	}

	name() {
		const name = this.props.data.name;

		return name.length > 19 ? `${name.substring(0, 18)}...` : name;
	}

	render() {
		return (
			<div
				className="entity"
				key={this.props.data.path}
				id={`folder-${this.props.data.id}`}
			>
				{this.contextMenu()}
				<div onClick={this.props.whenClicked}>
					<div className="entity-icon">
						<i className="fas fa-folder"/>
					</div>
					<div className="entity-name">
						<span>{this.name()}</span>
					</div>
				</div>
			</div>
		);
	}
}