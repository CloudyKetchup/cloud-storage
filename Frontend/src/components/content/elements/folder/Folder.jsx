import React, { Component } from 'react';

import ContextMenu 	from '../contextmenu/ContextMenu'

const contextMenuListener = async (e, obj) => {
	e.preventDefault();
	obj.setState({ contextMenuShow : true });

	obj.props.parent.setState({ disableContextMenu : true });

	window.addEventListener('click', () => windowClickListener(obj), false);
}

const windowClickListener = async (obj) => {
	obj.setState({ contextMenuShow : false });

	obj.props.parent.setState({ disableContextMenu : false });
}

export default class Folder extends Component {
	state = { contextMenuShow : false };

	componentDidMount() {
		document.getElementById(`folder-${this.props.data.id}`)
			.addEventListener('contextmenu', e => contextMenuListener(e, this), false);
	}

	componentWillUnmount() {
		document.getElementById(`folder-${this.props.data.id}`)
			.removeEventListener('contextmenu', e => contextMenuListener(e, this), false);
	}

	contextMenu() {
		if (this.state.contextMenuShow) {
			return 	<ContextMenu
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