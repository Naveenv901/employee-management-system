import  React, { useState } from 'react';
import './styles/PropComponent.css';
function PropComponent(props) {

    const[count, setCount] = useState(0);
    return (
        <div className='prop-container'>
            {/* props  */}
            <h2>Prop Component</h2>
            <p>Name: {props.name}</p>
            <p>Age: {props.age}</p>
            <p>City: {props.city}</p>
            <div style={{display:"flex",padding:10}}>
            
            {/* state */}
            <button onClick={() => setCount(count + 1)} style={{padding:0}}>Increment Count</button>
            <p>Count: {count}</p>
            <button onClick={() => setCount(count - 1)}>Decrement Count</button>
            </div>

            {/* conditional rendering */}
            <div>
                <h2>Conditional Rendering</h2>
                {count % 2 === 0 ? <p>Count is even</p> : <p>Count is odd</p>}
            </div>

            
        </div>
    );
}

export default PropComponent;