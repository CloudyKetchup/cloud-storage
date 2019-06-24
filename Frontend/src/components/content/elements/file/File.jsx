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

export default class File extends Component {
	state = { contextMenuShow : false };

	componentDidMount() {
		document.getElementById(`file-${this.props.data.id}`)
			.addEventListener('contextmenu', e => contextMenuListener(e, this), false);
	}

	componentWillUnmount() {
		document.getElementById(`file-${this.props.data.id}`)
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

		return name.length > 18 ? `${name.substring(0, 17)}...` : name;
	}

	render() {
		return (
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
}