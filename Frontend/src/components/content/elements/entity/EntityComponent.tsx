import React, { Component } from 'react';

import { Entity } 		from '../../../../model/entity/Entity';
import App 				from '../../../../App';
import ContentContainer from '../../container/ContentContainer';
import ContextMenu 		from "../contextmenu/ContextMenu";

export type EntityProps = {
	parent: ContentContainer
	mainParent: App
	data: Entity
	handleAction: (action: string) => void
}

export default abstract class EntityComponent<EntityProps> extends Component<EntityProps> {
	state = {
		contextMenuShow : false,
		contextMenuStyle : {
			top: '',
			left: ''
		}
	};

	contextMenu = (data: Entity, handleAction: (action: string) => void, app : App) => {
		if (this.state.contextMenuShow) {
			return 	<ContextMenu
				parent={data}
				action={handleAction}
				onStart={() => app.setState({ elementSelected : data })}
				style={this.state.contextMenuStyle}
			/>
		}
	};

	name = (name: string) => {
		return name.length > 18 ? `${name.substring(0, 17)}...` : name;
	};
}
