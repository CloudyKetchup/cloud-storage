import React, {Component, CSSProperties, FC} from "react";

import { UploadingContextInterface }    from '../../context/UploadingContext';
import { ContextHelpers }               from '../../helpers';
import { UploadItem }                   from '../../model/UploadItem';
import UploadItemComponent              from './UploadItemComponent';

import './uploading-pane.css';

type IState = {
	hide : boolean
	items : UploadItem[]
	uploading : boolean
};

export let UploadingContextImpl : UploadingContextInterface;

const EmptyPane : FC<{ style? : CSSProperties }> = props => (
	<div style={props.style}>
		<i style={{ fontSize : 30, minHeight : 50 }} className="fas fa-inbox"/>
		<span>Empty here</span>
	</div>
);

export default class UploadingPane extends Component {
	state : IState = {
		hide : false,
		items : [],
		uploading : false
	};

	UNSAFE_componentWillMount = () => {
		UploadingContextImpl = ContextHelpers.createUploadContext(this);

		this.setState({ items : UploadingContextImpl.uploads });
	};

	shouldComponentUpdate = (nextState : Readonly<IState>) : boolean => this.state !== nextState;

	close = () => UploadingContextImpl.clearAllUploads && UploadingContextImpl.clearAllUploads();

	render = () => {
		if (this.state.items.length > 0) {
			return (
				<div className="uploading-pane" style={{ opacity : 1 }}>
					<div className="uploading-pane-header">
						<div style={{ width: "fit-content", margin: "unset" }}>
							<span>Upload</span>
						</div>
						<div style={{ display: "flex", marginLeft: "auto", marginRight: 10 }}>
							{
								!this.state.uploading
								&&
                                <button style={{ marginLeft: "auto" }} onClick={this.close}>
                                    <i className="fas fa-times"/>
                                </button>
							}
							<button style={{ marginLeft: "auto" }}
							        onClick={() => this.setState({ hide: !this.state.hide })}>
								<i style={{ transform: this.state.hide ? "rotate(-180deg)" : "" }}
								   className="fas fa-chevron-up"/>
							</button>
						</div>
					</div>
					<div style={this.state.hide ? { height: 0, opacity: 0, paddingTop: 0, paddingBottom: 0 } : { height: "",  opacity: 1 }}>
						{
							this.state.items.length === 0
								?
								<EmptyPane style={{ display: this.state.hide ? "none" : "grid", textAlign: "center" }}/>
								:
								<div className="uploading-pane-content"
								     style={{ display: this.state.hide ? "none" : "" }}>
									{this.state.items.map(data => <UploadItemComponent key={`upload-item-${data.id} ${data.progress}`} data={data}/>)}
								</div>
						}
					</div>
				</div>
			);
		}
		return <div/>;
	};
}
